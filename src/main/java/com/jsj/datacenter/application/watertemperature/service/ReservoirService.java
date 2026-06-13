package com.jsj.datacenter.application.watertemperature.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.adapter.temp.*;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlanFile;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanFileMapper;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanMapper;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import com.jsj.datacenter.infrastructure.vo.CalculateViewVO;
import com.jsj.datacenter.util.StringUtils;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.AxesChartStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.jsj.datacenter.infrastructure.common.enums.UploadFileType.*;

@Service
@Slf4j
public class ReservoirService {

    @Autowired
    private WaterTemperaturePlanService waterTemperaturePlanService;

    @Autowired
    private WaterTemperaturePlanMapper waterTemperaturePlanMapper;

    @Autowired
    private WaterTemperaturePlanFileMapper waterTemperaturePlanFileMapper;

    @Autowired
    private FileItemMapper fileItemMapper;

    @Autowired
    private Executor threadPoolTaskExecutor;

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private Color XChatBackgroundColor;
    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB直接内存缓冲区
    private static final byte LINE_FEED = (byte) '\n';
    private static final Random random = new Random();


    public WaterTemperaturePlan calculate(RiverCalculateVO riverCalculate) {
        WaterTemperaturePlan waterTemperaturePlan = null;
        if (riverCalculate.getPlanType() == 2){
             waterTemperaturePlan = waterTemperaturePlanMapper.selectListByPlanTypeOne(riverCalculate.getPlanType());
        }
        if (waterTemperaturePlan == null) {
            waterTemperaturePlan = new WaterTemperaturePlan();
            waterTemperaturePlan.setPlanName(riverCalculate.getPlanName());
            waterTemperaturePlan.setPlanType(riverCalculate.getPlanType());
            waterTemperaturePlan.setCreateTime(new Date());
            waterTemperaturePlan.setUpdateTime(new Date());
            waterTemperaturePlan.setMecovertcoe(riverCalculate.getMecovertcoe());
            waterTemperaturePlan.setSolarcoe(riverCalculate.getSolarcoe());
            waterTemperaturePlan.setWindcoe(riverCalculate.getWindcoe());
            waterTemperaturePlan.setTotalDay(riverCalculate.getTotalDay());
            waterTemperaturePlan.setProgressStatus(1);
            waterTemperaturePlan.setProcess("任务开始计算");
            waterTemperaturePlan.setPlanStartTime(riverCalculate.getPlanStartTime());
            waterTemperaturePlan.setPlanEndTime(riverCalculate.getPlanEndTime());
            waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
            waterTemperaturePlanMapper.insert(waterTemperaturePlan);
        } else {
            waterTemperaturePlan.setMecovertcoe(riverCalculate.getMecovertcoe());
            waterTemperaturePlan.setSolarcoe(riverCalculate.getSolarcoe());
            waterTemperaturePlan.setWindcoe(riverCalculate.getWindcoe());
            waterTemperaturePlan.setProgressStatus(1);
            waterTemperaturePlan.setProcess("任务开始计算");
            waterTemperaturePlan.setTotalDay(riverCalculate.getTotalDay());
            waterTemperaturePlan.setCreateTime(new Date());
            waterTemperaturePlan.setUpdateTime(new Date());
            waterTemperaturePlan.setPlanStartTime(riverCalculate.getPlanStartTime());
            waterTemperaturePlan.setPlanEndTime(riverCalculate.getPlanEndTime());
            waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
            waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        }
        saveBoundaryConditions(riverCalculate, waterTemperaturePlan);
        WaterTemperaturePlan finalWaterTemperaturePlan = waterTemperaturePlan;
        threadPoolTaskExecutor.execute(() -> {
            try {
                handleInputFile(finalWaterTemperaturePlan, riverCalculate);
                waterTemperaturePlanFileMapper.deleteByPlanIdAndType(finalWaterTemperaturePlan.getId(), 2);
                executeScript(finalWaterTemperaturePlan);
                List<SequenceWaterTem> sequenceWaterTems = null;
                Map<Integer, List<AlongWayWaterTem>> compListMap = null;
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(riverCalculate.getHdComparisonFileKey())) {
                    sequenceWaterTems = handleComparisonDataSequence(riverCalculate);
                    compListMap = handleComparisonDataAlong(riverCalculate);
                }
                this.executorDataHandle(finalWaterTemperaturePlan);
                handleData(finalWaterTemperaturePlan, sequenceWaterTems, compListMap);
                finalWaterTemperaturePlan.setProgressStatus(6);
                finalWaterTemperaturePlan.setProcess("任务执行完成");
                finalWaterTemperaturePlan.setUpdateTime(new Date());
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                finalWaterTemperaturePlan.setProgressStatus(-3);
                finalWaterTemperaturePlan.setProcess("任务执行异常");
                finalWaterTemperaturePlan.setUpdateTime(new Date());
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
            }
        });
        return waterTemperaturePlan;
    }

    private void handleResidualExcel(ZipOutputStream zipOutputStream, WaterTemperaturePlan finalWaterTemperaturePlan) throws IOException {
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileListByPlanId(finalWaterTemperaturePlan.getId());
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        List<FileItemPO> outExcelFiles = fileItemPOS.stream().filter(
                        fileItemPO -> fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RESERVOIR_COMP)
                                ||  fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RESER_OUT_RESULT_EXCEL))
                .collect(Collectors.toList());

        FileItemPO fileItemPOResult = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RESER_OUT_RESULT_EXCEL))
                .findFirst().orElse(null);
        FileItemPO fileItemPOCom = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RESERVOIR_COMP))
                .findFirst().orElse(null);
        if (fileItemPOCom == null){
            return;
        }
        String rootDir = System.getProperty("user.dir");
        String fileKeyDiff = UUID.randomUUID().toString();
        String file_dir_out_diff = rootDir + "/script/ricen/dyout/"+fileKeyDiff+".xlsx";
        //计算方差，残差
        ExcelWriter build = EasyExcel.write(file_dir_out_diff).build();
        assert fileItemPOResult != null;
        hdAlongWayWater(fileItemPOResult.getPath(), fileItemPOCom.getPath(), build);
        hdSequenceWater(fileItemPOResult.getPath(), fileItemPOCom.getPath(), build);
        build.finish();
        ZipEntry zipEntry = new ZipEntry("河道模型计算结果误差校验.xlsx");
        Path path = Paths.get(file_dir_out_diff);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(Files.readAllBytes(path));
        zipOutputStream.closeEntry();
    }


    public void hdAlongWayWater(String pathResult, String pathCom, ExcelWriter excelWriter){
        Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMap = Maps.newHashMap();
        EasyExcel.read(pathResult)
                .registerReadListener(new AlongWayWaterTemReadListener(alongWayWaterTemMap))
                .headRowNumber(4)
                //.head(AlongWayWaterTem.class)
                .sheet(1)
                .doReadSync();

        Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMapDiff = Maps.newHashMap();
        EasyExcel.read(pathCom)
                .registerReadListener(new AlongWayWaterTemReadListener(alongWayWaterTemMapDiff))
                .headRowNumber(4)
                //.head(AlongWayWaterTem.class)
                .sheet(1)
                .doReadSync();

        Iterator<Map.Entry<Integer, List<AlongWayWaterTem>>> iterator = alongWayWaterTemMap.entrySet().iterator();
        List<List<Double>> dataR = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<AlongWayWaterTem>> entry = iterator.next();
            Integer key = entry.getKey();
            List<AlongWayWaterTem> alongWayWaterTems = entry.getValue();
            List<AlongWayWaterTem> alongWayWaterTemsDiff = alongWayWaterTemMapDiff.get(key);
            List<Double> waterTems = Lists.newArrayList();
            for (AlongWayWaterTem alongWayWaterTem : alongWayWaterTems) {
                AlongWayWaterTem alongWayWaterComTem = alongWayWaterTemsDiff.stream()
                        .filter(item -> Objects.equals(item.getDistance(), alongWayWaterTem.getDistance()))
                        .findFirst().orElse(null);
                if (alongWayWaterComTem != null) {
                    waterTems.add(alongWayWaterTem.getWaterTem()- alongWayWaterComTem.getWaterTem());
                }
            }
            dataR.add(waterTems);
        }
        Set<Map.Entry<Integer, List<AlongWayWaterTem>>> entries = alongWayWaterTemMap.entrySet();
        List<List<String>> head = Lists.newArrayList();
        for (Map.Entry<Integer, List<AlongWayWaterTem>> entry : entries) {
            List<String> headItem = Lists.newArrayList();
            headItem.add(entry.getKey().toString());
            head.add(headItem);
        }
        List<List<Object>> data = Lists.newArrayList();
        //方差
        for (int i = 0; i < dataR.size(); i++) {
            List<Double> residualSqCollectS = dataR.get(i).stream()
                    .map(item -> Math.pow(item, 2))
                    .collect(Collectors.toList());
            double sum = residualSqCollectS.stream().mapToDouble(Double::doubleValue).sum();
            data.add(Arrays.asList("第" +(i+1)+ "天方差", sum));
        }

        WriteSheet excelWriterSheetR = EasyExcel.writerSheet(0, "水温沿程变化-残差").head(head).build();
        WriteSheet excelWriterSheetF = EasyExcel.writerSheet(1, "水温沿程变化-方差").build();
        excelWriter.write(transposeMatrix(dataR), excelWriterSheetR);
        excelWriter.write(data, excelWriterSheetF);

    }

    public static <T> List<List<T>> transposeMatrix(List<List<T>> matrix) {
        return IntStream.range(0, matrix.get(0).size())
                .mapToObj(i -> matrix.stream()
                        .map(row -> row.get(i))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public void hdSequenceWater(String pathResult, String pathCom, ExcelWriter excelWriter){
        List<List<String>> head = Lists.newArrayList();
        List<SequenceWaterTem> sequenceWaterTems = EasyExcel.read(pathResult, new SequenceWaterTemReadListener(head))
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();
        List<SequenceWaterTem> sequenceWaterComTems = EasyExcel.read(pathCom)
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();
        List<AlongWayWaterAlongResidualTem> sequenceWaterResidualTems = Lists.newArrayList();
        for (SequenceWaterTem sequenceWaterTem : sequenceWaterTems) {
            SequenceWaterTem sequenceWaterComTem = sequenceWaterComTems.stream()
                    .filter(item -> Objects.equals(item.getDay(), sequenceWaterTem.getDay()))
                    .findFirst().orElse(null);
            if (sequenceWaterComTem != null) {
                AlongWayWaterAlongResidualTem sequenceWaterTemResidual = new AlongWayWaterAlongResidualTem();
                sequenceWaterTemResidual.setDay(sequenceWaterTem.getDay());
                sequenceWaterTemResidual.setS1(sequenceWaterTem.getS1()-sequenceWaterComTem.getS1());
                sequenceWaterTemResidual.setS2(sequenceWaterTem.getS2()-sequenceWaterComTem.getS2());
                sequenceWaterResidualTems.add(sequenceWaterTemResidual);
            }
        }

        //方差逻辑
        List<Double> residualSqCollect = sequenceWaterResidualTems.stream()
                .map(item -> Math.pow(item.getS1(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);

        //方差逻辑
        List<Double> residualSqCollectS = sequenceWaterResidualTems.stream()
                .map(item -> Math.pow(item.getS2(), 2))
                .collect(Collectors.toList());
        double sum2 = residualSqCollectS.stream().mapToDouble(Double::doubleValue).sum();
        sum2 = sum2/residualSqCollect.size();                     //方差结果
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);

        List<String> strings = head.get(1);
        List<String> strings1 = head.get(2);
        double sum3 = (sum+sum2);
        List<List<Object>> data = Arrays.asList(
                Arrays.asList(strings.get(0)+"方差", sum),
                Arrays.asList(strings1.get(0)+"方差", sum2),
                Arrays.asList("总方差", sum3)
        );
        WriteSheet excelWriterSheetR = EasyExcel.writerSheet(2, "断面水温变化-残差").head(head).build();
        WriteSheet excelWriterSheetF = EasyExcel.writerSheet(3, "断面水温变化-方差").build();
        excelWriter.write(sequenceWaterResidualTems, excelWriterSheetR);
        excelWriter.write(data, excelWriterSheetF);
    }

    private List<SequenceWaterTem> handleComparisonDataSequence(RiverCalculateVO riverCalculate) {
        FileItemPO file = fileItemMapper.getFile(riverCalculate.getHdComparisonFileKey());
        String path = file.getPath();
        return EasyExcel.read(path)
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();

    }
     private Map<Integer, List<AlongWayWaterTem>> handleComparisonDataAlong(RiverCalculateVO riverCalculate) {
        FileItemPO file = fileItemMapper.getFile(riverCalculate.getHdComparisonFileKey());
        String path = file.getPath();
        Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMap = Maps.newHashMap();
        EasyExcel.read(path)
                .registerReadListener(new AlongWayWaterTemReadListener(alongWayWaterTemMap))
                .headRowNumber(4)
                //.head(AlongWayWaterTem.class)
                .sheet(1)
                .doReadSync();
        return alongWayWaterTemMap;
    }

    private void saveBoundaryConditions(RiverCalculateVO riverCalculate, WaterTemperaturePlan waterTemperaturePlan) {
        waterTemperaturePlan.setProgressStatus(2);
        waterTemperaturePlan.setProcess("任务输入参数保存");
        waterTemperaturePlan.setUpdateTime(new Date());
        waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        waterTemperaturePlanFileMapper.deleteByPlanIdAndType(waterTemperaturePlan.getId(), 1);
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(riverCalculate.getHdWeatherFileKey());
        waterTemperaturePlanFile.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFile.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);

        WaterTemperaturePlanFile waterTemperaturePlanFileMainStreamInflow = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileMainStreamInflow.setFileKey(riverCalculate.getMainStreamInflowFileKey());
        waterTemperaturePlanFileMainStreamInflow.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileMainStreamInflow.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileMainStreamInflow);

        WaterTemperaturePlanFile waterTemperaturePlanFileMainStreamTemperature = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileMainStreamTemperature.setFileKey(riverCalculate.getMainStreamTemperatureFileKey());
        waterTemperaturePlanFileMainStreamTemperature.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileMainStreamTemperature.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileMainStreamTemperature);

        WaterTemperaturePlanFile waterTemperaturePlanFileBranchStreamInflow = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileBranchStreamInflow.setFileKey(riverCalculate.getBranchStreamInflowFileKey());
        waterTemperaturePlanFileBranchStreamInflow.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileBranchStreamInflow.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileBranchStreamInflow);

        WaterTemperaturePlanFile waterTemperaturePlanFileBranchStreamTemperature = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileBranchStreamTemperature.setFileKey(riverCalculate.getBranchStreamTemperatureFileKey());
        waterTemperaturePlanFileBranchStreamTemperature.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileBranchStreamTemperature.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileBranchStreamTemperature);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(riverCalculate.getHdComparisonFileKey())){
            WaterTemperaturePlanFile waterTemperaturePlanFileComp = new WaterTemperaturePlanFile();
            waterTemperaturePlanFileComp.setFileKey(riverCalculate.getHdComparisonFileKey());
            waterTemperaturePlanFileComp.setPlanId(waterTemperaturePlan.getId());
            waterTemperaturePlanFileComp.setFileType(1);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileComp);
        }
    }

    /**
     * 处理脚本参数处理编写
     */
    public void handleInputFile(WaterTemperaturePlan waterTemperaturePlan, RiverCalculateVO riverCalculate) throws IOException {
        waterTemperaturePlan.setProgressStatus(3);
        waterTemperaturePlan.setProcess("参数文件解析");
        waterTemperaturePlan.setUpdateTime(new Date());
        waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        String weatherFile = "";
        String mainInflowFile = "";
        String mainTempFile = "";
        String branchInflowFile = "";
        String branchTempFile = "";
        List<String> fileKeys = Lists.newArrayList();
        fileKeys.add(riverCalculate.getHdWeatherFileKey());
        fileKeys.add(riverCalculate.getMainStreamInflowFileKey());
        fileKeys.add(riverCalculate.getMainStreamTemperatureFileKey());
        fileKeys.add(riverCalculate.getBranchStreamInflowFileKey());
        fileKeys.add(riverCalculate.getBranchStreamTemperatureFileKey());
        //List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(waterTemperaturePlan.getId(), 1);
        if (CollectionUtils.isNotEmpty(fileKeys)) {
            List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
            if (CollectionUtils.isNotEmpty(fileItemPOS)) {
                for (FileItemPO fileItemPO : fileItemPOS) {
                    switch (fileItemPO.getFileType()) {
                        case WA_SCHEMA_PARAM_WEATHER_RESERVOIR:
                            weatherFile = fileItemPO.getPath();
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW:
                            mainInflowFile = fileItemPO.getPath();
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE:
                            mainTempFile = fileItemPO.getPath();
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW:
                            branchInflowFile = fileItemPO.getPath();
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE:
                            branchTempFile = fileItemPO.getPath();
                            break;
                    }
                }
            }

        }
        this.changeReadFile(waterTemperaturePlan);
        this.generateWeatherFile(weatherFile, mainTempFile);
        this.generateInflowFile(mainInflowFile, branchInflowFile, branchTempFile);
    }

    public void changeReadFile(WaterTemperaturePlan waterTemperaturePlan) throws IOException {
        log.info("修改read79.d文件参数：solarcoe-{}，mecovertcoe-{}，windcoe-{}", waterTemperaturePlan.getSolarcoe(), waterTemperaturePlan.getMecovertcoe(), waterTemperaturePlan.getWindcoe());
        String rootDir = System.getProperty("user.dir");
        String file_dir_out = rootDir + "/script/ricen/input/read79.d";
        Path path = Paths.get(file_dir_out);
        List<String> readAllLines = Files.readAllLines(path);
        Double solarcoe = waterTemperaturePlan.getSolarcoe()!=null?waterTemperaturePlan.getSolarcoe():0;
        Double mecovertcoe = waterTemperaturePlan.getMecovertcoe()!=null?waterTemperaturePlan.getMecovertcoe():0;
        Double windcoe = waterTemperaturePlan.getWindcoe()!=null?waterTemperaturePlan.getWindcoe():0;
        StringBuffer stringBufferLine = new StringBuffer();
        stringBufferLine.append(StringUtils.alignLeft(String.valueOf(solarcoe), 8));
        stringBufferLine.append(StringUtils.alignLeft(String.valueOf(mecovertcoe), 8));
        stringBufferLine.append(StringUtils.alignLeft(String.valueOf(windcoe), 8));
        readAllLines.set(1, stringBufferLine.toString());
        Files.write(path, readAllLines);
    }

    private void generateInflowFile(String mainInflowFile, String branchInflowFile, String branchTempFile) throws IOException {
        String rootDir = System.getProperty("user.dir");
        List<String> head = Lists.newArrayList();
        List<RiverChannelFlowTem> riverChannelMainWaterTemps = EasyExcel.read(mainInflowFile, new DynamicColumnListener(head))
                .head(RiverChannelFlowTem.class)
                .headRowNumber(2).sheet().doReadSync();
        List<Map<Integer, String>> branchFlows = EasyExcel.read(branchInflowFile, new DynamicMapColumnListener())
                .sheet().doReadSync();
        List<Map<Integer, String>> branchTems = EasyExcel.read(branchTempFile, new DynamicMapColumnListener())
                .sheet().doReadSync();

        String file_dir_out = rootDir + "/script/ricen/input/hyd79.d";
        Path path = Paths.get(file_dir_out);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        String oneLine = bufferedReader.readLine();
        BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
        bufferedWriter.write(oneLine);
        bufferedWriter.newLine();
        StringBuffer stringHeadBuffer = new StringBuffer();
        head.forEach(item -> {
            stringHeadBuffer.append(StringUtils.alignRight(item, 10));
        });
        bufferedWriter.write(stringHeadBuffer.toString());
        bufferedWriter.newLine();
        for (int i = 0; i < riverChannelMainWaterTemps.size(); i++) {
            StringBuffer stringBuffer = new StringBuffer();
            RiverChannelFlowTem riverChannelFlowTem = riverChannelMainWaterTemps.get(i);
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(riverChannelFlowTem.getDateIndex()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(riverChannelFlowTem.getUpstreamWaterLevel()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(riverChannelFlowTem.getUpstreamFlow()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(riverChannelFlowTem.getDownstreamWaterLevel()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(riverChannelFlowTem.getDownstreamFlow()), 10));
            branchFlows.get(i).values().forEach(item -> {
                stringBuffer.append(StringUtils.alignLeft(item, 10));
            });
            branchTems.get(i).values().forEach(item -> {
                stringBuffer.append(StringUtils.alignLeft(item, 10));
            });
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
    }

    private void generateWeatherFile(String weatherFile, String mainInflowFile) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_out = rootDir + "/script/ricen/input/ymet79.d";
        Path path = Paths.get(file_dir_out);
        List<String> headList = this.generateWeatherFileHead();
        BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
        List<RiverChannelMainWaterTemp> RiverChannelMainWaterTemps = EasyExcel.read(mainInflowFile)
                .head(RiverChannelMainWaterTemp.class)
                .sheet()
                .doReadSync();
        List<RiverChannelMeteorologyTem> RiverChannelMeteorologyTems = EasyExcel.read(weatherFile, RiverChannelMeteorologyTem.class,
                        new RiverChannelMeteorologyListener(RiverChannelMainWaterTemps))
                .sheet()
                .doReadSync();

        if (CollectionUtils.isNotEmpty(headList)) {
            for (String head : headList) {
                bufferedWriter.write(head);
                bufferedWriter.newLine();
            }
        }
        for (RiverChannelMeteorologyTem tem : RiverChannelMeteorologyTems) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getDataIndex()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getRainfall()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getTemp()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getRelativeHumidity()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getWindSpeed()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getWindDirection()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getCloudAmount()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getSolarRadiation()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getWaterTemp()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getSurfaceIce()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getInnerIce()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getSurfaceIceThickness()), 10));
            stringBuffer.append(StringUtils.alignLeft(String.valueOf(tem.getInnerIceDensity()), 10));
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
    }

    private List<String> generateWeatherFileHead() throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_out = rootDir + "/script/ricen/dyout/ymet79.d";
        Path path = Paths.get(file_dir_out);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        String oneLine = bufferedReader.readLine();
        String twoLine = bufferedReader.readLine();
        String threeLine = bufferedReader.readLine();
        String fourLine = bufferedReader.readLine();
        String fiveLine = bufferedReader.readLine();
        String sixLine = bufferedReader.readLine();
        List<String> headList = Lists.newArrayList();
        headList.add(oneLine);
        headList.add(twoLine);
        headList.add(threeLine);
        headList.add(fourLine);
        headList.add(fiveLine);
        headList.add(sixLine);
        /*bufferedWriter.write(oneLine);
        bufferedWriter.newLine();
        bufferedWriter.write(twoLine);
        bufferedWriter.newLine();
        bufferedWriter.write(threeLine);
        bufferedWriter.newLine();
        bufferedWriter.write(fourLine);
        bufferedWriter.newLine();
        bufferedWriter.write(fiveLine);
        bufferedWriter.newLine();
        bufferedWriter.write(sixLine);
        bufferedWriter.newLine();*/
        return headList;
    }

    /**
     * 执行计算脚本
     */
    public void executeScript(WaterTemperaturePlan waterTemperaturePlan) {
        waterTemperaturePlan.setProgressStatus(4);
        waterTemperaturePlan.setProcess("脚本开始执行");
        waterTemperaturePlan.setUpdateTime(new Date());
        waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        this.executeRICENScript(1);
        log.info("执行脚本1完成");
        this.executeRICENScript(0);
        log.info("执行脚本0完成");
    }

    private void executeRICENScript(int i) {
        try {
            String rootDir = System.getProperty("user.dir");
            String main_flow = rootDir + "/script/ricen/RICEN-1D.exe";
            // 创建ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(main_flow);
            File file = new File(main_flow);
            pb.directory(file.getParentFile());
            Process process = pb.start();

            // 获取输出流（用于向exe发送输入）
            OutputStream outputStream = process.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (org.apache.commons.lang3.StringUtils.isBlank(line)) {
                            writer.println(i);
                            writer.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待程序执行完成
            int exitCode = process.waitFor();
            System.out.println("程序退出代码: " + exitCode);
            writer.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据分装处理
     */
    public void handleData(WaterTemperaturePlan waterTemperaturePlan, List<SequenceWaterTem> sequenceWaterTems,
                           Map<Integer, List<AlongWayWaterTem>> compListMap) throws IOException {
        waterTemperaturePlan.setProgressStatus(5);
        waterTemperaturePlan.setProcess("数据加工处理中");
        waterTemperaturePlan.setUpdateTime(new Date());
        waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        //log.info("处理数据开始：{}, {}", JSONObject.toJSONString(sequenceWaterTems), JSONObject.toJSONString(compListMap));
        this.writeProfileWithCharBuffer(waterTemperaturePlan, compListMap);
        log.info("处理数据writeProfileWithCharBuffer结束");
        this.writeSequenceWithCharBuffer(waterTemperaturePlan, sequenceWaterTems);
        log.info("writeSequenceWithCharBuffer");


    }
    public void executorDataHandle(WaterTemperaturePlan waterTemperaturePlan) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String fileKey = UUID.randomUUID().toString();
        String file_dir_out = rootDir + "/script/ricen/dyout/"+fileKey+".xlsx";
        Path path_out = Paths.get(file_dir_out);
        //WriteCellStyle noStyle = new WriteCellStyle();
        //HorizontalCellStyleStrategy strategy = new HorizontalCellStyleStrategy(noStyle, noStyle);
        ExcelWriter excelWriter = EasyExcel.write(path_out.toFile()).inMemory(true).build();
        this.handleTimeSeq(excelWriter);
        this.handleProfile(excelWriter);
        excelWriter.finish();
        FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RESER_OUT_RESULT_EXCEL, "河道模型计算结果.xlsx", Files.newInputStream(path_out));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
        log.info("河道模型计算导出文件名：{}", file_dir_out);
    }

    private void handleTimeSeq(ExcelWriter excelWriter) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/script/ricen/Output/Time Sequence/Ice/twtm.dat";
        Path path = Paths.get(file_dir_input);
        List<String> dataLines = Files.readAllLines(path);
        String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        String s1 = dataLines.get(0);
        String s2 = dataLines.get(1);
        String s3 = dataLines.get(2);
        dataLines.remove(0);
        dataLines.remove(0);
        dataLines.remove(0);
        List<SequenceWaterTem> tems = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(dataLines)){
            for (int i = 1; i <= dataLines.size(); i++) {
                String line = dataLines.get(i - 1);
                System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
                if (Pattern.matches(pattern, line)){
                    System.out.println(line.trim().replaceAll("\\s+", "-"));
                    String trimLine = line.trim();
                    String[] items = trimLine.split("\\s+");
                    SequenceWaterTem sequenceWaterTem = new SequenceWaterTem();
                    sequenceWaterTem.setDay(Double.parseDouble(items[0]));
                    sequenceWaterTem.setS1(Double.parseDouble(items[1]));
                    sequenceWaterTem.setS2(Double.parseDouble(items[2]));
                    tems.add(sequenceWaterTem);
                } else  {
                }
            }
            List<List<String>> head = Lists.newArrayList();
            this.headSeqHandle(head, s1, s2, s3);
            WriteSheet sheet = EasyExcel.writerSheet(0, "断面水温").head(head).automaticMergeHead(false).build();
            excelWriter.write(tems, sheet);
        }
    }

    private void headSeqHandle(List<List<String>> head, String s1, String s2, String s3) {
        String regex = "\\s+";
        String[] head1Array = s1.split(regex);
        String[] head2Array = s2.split(regex);
        String[] head3Array = s3.split(regex);

        List<String> headCo1 = Lists.newArrayList();
        headCo1.add(head1Array[0]);
        headCo1.add(head2Array[0]);
        headCo1.add(head3Array[0]);
        headCo1.add("");
        List<String> headCo2 = Lists.newArrayList();
        headCo2.add(head1Array[1]);
        headCo2.add(head2Array[1]);
        headCo2.add(head3Array[1]);
        headCo2.add("输出天数");
        List<String> headCo3 = Lists.newArrayList();
        headCo3.add(head1Array[2]);
        headCo3.add(head2Array[2]);
        headCo3.add(head3Array[2]);
        headCo3.add(head2Array[3]+"断面的水温");
        List<String> headCo4 = Lists.newArrayList();
        headCo4.add(head1Array[3]);
        headCo4.add(head2Array[3]);
        headCo4.add(head3Array[3]);
        headCo4.add(head2Array[4]+"断面的水温");
        List<String> headCo5 = Lists.newArrayList();
        headCo5.add(head1Array[4]);
        headCo5.add(head2Array[4]);
        headCo5.add("");
        headCo5.add("");
        head.add(headCo1);
        head.add(headCo2);
        head.add(headCo3);
        head.add(headCo4);
        head.add(headCo5);
    }

    private void handleProfile(ExcelWriter excelWriter) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/script/ricen/Output/Profile/Ice/twdis.dat";
        Path path = Paths.get(file_dir_input);
        List<String> dataLines = Files.readAllLines(path);
        int[] indices = IntStream.range(0, dataLines.size())
                .filter(i -> dataLines.get(i).startsWith("Zone"))
                .toArray();
        String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        String regex = "\\s+";
        WriteSheet sheet = EasyExcel.writerSheet(1, "沿程水温")
                .head(AlongWayWaterTem.class)
                .useDefaultStyle(false)
                .build();
        for (int i = 0; i < indices.length; i++) {
            int indexStart = indices[i];
            int indexEnd;
            if(i == indices.length-1){
                indexEnd = dataLines.size();
            } else {
                indexEnd = indices[i+1];
            }
            List<String> subList = dataLines.subList(indexStart, indexEnd);
            List<Object> tems = Lists.newArrayList();
            List<List<String>> head = Lists.newArrayList();
            for (int j = 1; j <= subList.size(); j++) {
                String line = subList.get(j - 1);
                System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
                if (Pattern.matches(pattern, line)){
                    System.out.println(line.trim().replaceAll("\\s+", "-"));
                    String trimLine = line.trim();
                    String[] items = trimLine.split("\\s+");
                    AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
                    alongWayWaterTem.setDistance(Double.parseDouble(items[0]));
                    alongWayWaterTem.setWaterTem(Double.parseDouble(items[1]));
                    alongWayWaterTem.setAirTem(Double.parseDouble(items[2]));
                    tems.add(alongWayWaterTem);
                } else  {
                    String[] split = line.split(regex);
                    for(String item: split){
                        List<String> headItem = Lists.newArrayList();
                        headItem.add(item);
                        head.add(headItem);
                    }
                }
            }
            WriteTable writeTable = EasyExcel.writerTable(i).head(head).build();
            excelWriter.write(tems, sheet,  writeTable);
        }
    }

    private void headProHandle(List<List<String>> head, String s1, String s2, String s3) {

        //TITLE = "WATER and Air TEMP. PROFILE"
        // VARIABLES  = X, Tw, Ta
        //Zone T="    5.00 days" I=  35
        String regex = "=";
        String[] headArray1 = s1.split(regex);
        String[] headArray2 = s2.split(regex);
        String[] headArray3 = s3.split(regex);

    }

    public void writeSequenceWithCharBuffer(WaterTemperaturePlan waterTemperaturePlan, List<SequenceWaterTem> sequenceWaterTems) throws IOException {
        String rootDir = System.getProperty("user.dir");
        Path filePath = Paths.get(rootDir + "/script/ricen/Output/Time Sequence/Ice/twtm.dat");
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            StringBuilder lineBuffer = new StringBuilder(256);
            List<String> keys = Lists.newArrayList();
            List<SequenceWaterTem> dataList = Lists.newArrayList();
            while (channel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    if (b == LINE_FEED) {
                        if (org.apache.commons.lang3.StringUtils.contains(lineBuffer.toString(), "TITLE")
                                || org.apache.commons.lang3.StringUtils.contains(lineBuffer.toString(), "VARIABLES")) {
                            if (org.apache.commons.lang3.StringUtils.contains(lineBuffer.toString(), "VARIABLES")) {
                                String[] split = lineBuffer.toString().split(",");
                                String[] split1 = split[1].split("\\s+");
                                keys.addAll(Arrays.stream(split1).filter(com.baomidou.mybatisplus.core.toolkit.StringUtils::isNotEmpty).collect(Collectors.toList()));
                            }
                            lineBuffer.setLength(0);
                            continue;
                        }
                        processSequenceLine(lineBuffer.toString(), dataList);
                        lineBuffer.setLength(0);
                    } else {
                        lineBuffer.append((char) b);
                    }
                }
                buffer.clear();
            }
            // 处理最后一行（无换行符结尾的情况）
            if (lineBuffer.length() > 0) {
                processSequenceLine(lineBuffer.toString(), dataList);
            }
            log.info("generateSequenceImage开始：+{}", keys);
            this.generateSequenceImage(dataList, keys, waterTemperaturePlan.getId(),sequenceWaterTems);
            log.info("generateSequenceImage结束");

        }
    }

    private void generateSequenceImage(List<SequenceWaterTem> dataList, List<String> keys, Integer planId, List<SequenceWaterTem> sequenceWaterTems) throws IOException {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        //计算结果
        XYChart chart = new XYChartBuilder()
                .width(1320)
                .height(460)
                .title("断面水温")
                .xAxisTitle("时间（天）")
                .yAxisTitle("断面水温（° C）")
                .build();
        this.setChartStyle(chart);
        List<Double> collectsy_s1 = dataList.stream().map(SequenceWaterTem::getS1).collect(Collectors.toList());
        List<Double> collectsy_s2 = dataList.stream().map(SequenceWaterTem::getS2).collect(Collectors.toList());
        List<Double> collectx = dataList.stream().map(SequenceWaterTem::getDay).collect(Collectors.toList());
        Iterator<String> iterator = keys.iterator();
        String next1 = iterator.next();
        String next2 = iterator.next();
        XYSeries xySeries1Y = chart.addSeries(next1 + "断面", collectx, collectsy_s1);
        XYSeries xySeries2Y = chart.addSeries(next2 + "断面", collectx, collectsy_s2);
        this.setSeriesStyle(xySeries1Y, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
        this.setSeriesStyle(xySeries2Y, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
        FileItemDTO fileItemDTO = fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RESER_OUT_ALONG_WAY, "sequence.png", BitmapEncoder.getBufferedImage(chart));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
        if (CollectionUtils.isNotEmpty(sequenceWaterTems)) {
            XYChart chartDiff_s1 = new XYChartBuilder()
                    .width(1320)
                    .height(460)
                    .title("断面水温")
                    .xAxisTitle("时间（天）")
                    .yAxisTitle("断面水温（° C）")
                    .build();
            this.setChartStyle(chartDiff_s1);
            List<Double> collectsDiffy_s1 = sequenceWaterTems.stream().map(SequenceWaterTem::getS1).collect(Collectors.toList());
            List<Double> collectsDiffy_s2 = sequenceWaterTems.stream().map(SequenceWaterTem::getS2).collect(Collectors.toList());
            XYSeries xySeries = chartDiff_s1.addSeries(next1 + "断面", collectx, collectsy_s1);
            //XYSeries xySeries1 = chartDiff_s1.addSeries("实测" + next1 + "断面", collectx, collectsDiffy_s1);
            XYSeries xySeries2 = chartDiff_s1.addSeries(next2 + "断面", collectx, collectsy_s2);
            //XYSeries xySeries3 = chartDiff_s1.addSeries("实测" + next2 + "断面2", collectx, collectsDiffy_s2);

            this.setSeriesStyle(xySeries, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
            //this.setSeriesStyle(xySeries1, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
            this.setSeriesStyle(xySeries2, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
            //this.setSeriesStyle(xySeries3, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
            FileItemDTO fileItemDTODiff = fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_ALONG_WAY, "sequence_diff.png", BitmapEncoder.getBufferedImage(chartDiff_s1));
            WaterTemperaturePlanFile waterTemperaturePlanFileDiff = new WaterTemperaturePlanFile();
            waterTemperaturePlanFileDiff.setFileKey(fileItemDTODiff.getFileKey());
            waterTemperaturePlanFileDiff.setPlanId(planId);
            waterTemperaturePlanFileDiff.setFileType(2);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileDiff);
        }
    }

    private void setSeriesStyle(XYSeries xySeries, Color color) {
        xySeries.setMarker(SeriesMarkers.NONE);
        xySeries.setLineWidth(5);
        xySeries.setMarkerColor(Color.WHITE);
        xySeries.setLineColor(color);
    }

    private void processSequenceLine(String line, List<SequenceWaterTem> dataList) {
        if (!org.apache.commons.lang3.StringUtils.contains(line, "ZONE")) {
            SequenceWaterTem sequenceWaterTem = new SequenceWaterTem();
            String[] result = line.split("\\s+");
            sequenceWaterTem.setDay(Double.parseDouble(result[1]));
            sequenceWaterTem.setS1(Double.parseDouble(result[2]));
            sequenceWaterTem.setS2(Double.parseDouble(result[3]));
            dataList.add(sequenceWaterTem);
        }
    }

    public void writeProfileWithCharBuffer(WaterTemperaturePlan waterTemperaturePlan, Map<Integer, List<AlongWayWaterTem>> compListMap) throws IOException {
        String rootDir = System.getProperty("user.dir");
        Path filePath = Paths.get(rootDir + "/script/ricen/Output/Profile/ice/twdis.dat");
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            StringBuilder lineBuffer = new StringBuilder(256);
            Set<Integer> keys =  Sets.newHashSet();
            Map<Integer,List<AlongWayWaterTem>> dataStr = Maps.newHashMap();
            while (channel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    if (b == LINE_FEED) {
                        if (org.apache.commons.lang3.StringUtils.contains(lineBuffer.toString(), "TITLE")
                                || org.apache.commons.lang3.StringUtils.contains(lineBuffer.toString(), "VARIABLES")) {
                            lineBuffer.setLength(0);
                            continue;
                        }
                        processLine(lineBuffer.toString(), dataStr,keys);
                        lineBuffer.setLength(0);
                    } else {
                        lineBuffer.append((char) b);
                    }
                }
                buffer.clear();
            }
            // 处理最后一行（无换行符结尾的情况）
            if (lineBuffer.length() > 0) {
                processLine(lineBuffer.toString(), dataStr, keys);
            }
            log.info("generateProfileImage开始");
            generateProfileImage(dataStr, waterTemperaturePlan.getId(), compListMap);
            log.info("generateProfileImage结束");

        }
    }

    private void generateProfileImage(Map<Integer, List<AlongWayWaterTem>> dataStr, Integer planId, Map<Integer, List<AlongWayWaterTem>> compListMap) throws IOException {
        Iterator<Map.Entry<Integer, List<AlongWayWaterTem>>> iterator = null;
        if (CollectionUtils.isNotEmpty(compListMap)) {
            iterator = compListMap.entrySet().iterator();
        }
        String rootDir = System.getProperty("user.dir");
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (Map.Entry<Integer, List<AlongWayWaterTem>> entry : dataStr.entrySet()) {
            XYChart chart = new XYChartBuilder().width(1320).height(460).title("沿程水温").xAxisTitle("断面累距（m）").yAxisTitle("水温（° C）").build();
            chart.getStyler().setYAxisMin(0.0);
            chart.getStyler().setYAxisMax(18.0);
            chart.getStyler().setXAxisMin(0.0);
            chart.getStyler().setXAxisMax(80.0);
            this.setChartStyle(chart);
            XYChart chartComp = new XYChartBuilder().width(1320).height(460).title("沿程水温").xAxisTitle("断面累距（m）").yAxisTitle("水温（° C）").build();
            chartComp.getStyler().setYAxisMin(0.0);
            chartComp.getStyler().setYAxisMax(18.0);
            chartComp.getStyler().setXAxisMin(0.0);
            chartComp.getStyler().setXAxisMax(80.0);
            this.setChartStyle(chartComp);
            List<Double> collectResultY = entry.getValue().stream().map(AlongWayWaterTem::getWaterTem).collect(Collectors.toList());
            List<Double> collectResultX = entry.getValue().stream().map(AlongWayWaterTem::getDistance).collect(Collectors.toList());
            XYSeries xySeries = chart.addSeries(entry.getKey() + "天", collectResultX, collectResultY);
            this.setSeriesStyle(xySeries,new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
            bufferedImages.add(BitmapEncoder.getBufferedImage(chart));
            FileItemDTO fileItemDTO = fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RESER_OUT_PROFILE, "profile_" + entry.getKey() + ".png", BitmapEncoder.getBufferedImage(chart));
            WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
            waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
            waterTemperaturePlanFile.setPlanId(planId);
            waterTemperaturePlanFile.setFileType(2);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
            if (compListMap != null) {
                assert iterator != null;
                Map.Entry<Integer, List<AlongWayWaterTem>> compListEntry = iterator.next();
                List<Double> collectCompY = compListEntry.getValue().stream().map(AlongWayWaterTem::getWaterTem).collect(Collectors.toList());
                List<Double> collectCompX = compListEntry.getValue().stream().map(AlongWayWaterTem::getDistance).collect(Collectors.toList());
                XYSeries xySeriesComp1 = chartComp.addSeries("第" + entry.getKey() + "天", collectResultX, collectResultY);
                XYSeries xySeriesComp2 = chartComp.addSeries("实测"+compListEntry.getKey() + "天", collectCompX, collectCompY);
                this.setSeriesStyle(xySeriesComp1, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
                this.setSeriesStyle(xySeriesComp2, new Color(random.nextInt(128) + 128, random.nextInt(128) + 128, random.nextInt(128) + 128));
                FileItemDTO fileItemCompDTO = fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_PROFILE, "profile_comp_" + entry.getKey() + ".png", BitmapEncoder.getBufferedImage(chartComp));
                WaterTemperaturePlanFile waterTemperatureCompPlanFile = new WaterTemperaturePlanFile();
                waterTemperatureCompPlanFile.setFileKey(fileItemCompDTO.getFileKey());
                waterTemperatureCompPlanFile.setPlanId(planId);
                waterTemperatureCompPlanFile.setFileType(2);
                waterTemperaturePlanFileMapper.insert(waterTemperatureCompPlanFile);
            }
        }
        AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
        animatedGifEncoder.start(rootDir + "/script/ricen/dyout/profile_image.gif");
        animatedGifEncoder.setSize(1320, 460);
        //图片之间间隔时间
        animatedGifEncoder.setDelay(1000);
        //重复次数 0表示无限重复 默认不重复
        animatedGifEncoder.setRepeat(0);
        animatedGifEncoder.setQuality(10);
        for (BufferedImage bufferedImage : bufferedImages) {
            animatedGifEncoder.addFrame(bufferedImage);
        }
        animatedGifEncoder.finish();
        File fileGif = Paths.get(rootDir + "/script/ricen/dyout/profile_image.gif").toFile();
        FileItemDTO fileItemDTO = fileItemService.saveFileData(UploadFileType.WA_SCHEMA_PARAM_RESER_OUT_GIF,fileGif);
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
    }



    private void processLine(String line, Map<Integer,List<AlongWayWaterTem>> dataStr, Set<Integer> keys) throws IOException {
        if (!org.apache.commons.lang3.StringUtils.contains(line, "Zone T=")) {
            AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
            String[] result = line.split("\\s+");
            alongWayWaterTem.setDistance(Double.valueOf(result[1]));
            alongWayWaterTem.setWaterTem(Double.valueOf(result[2]));
            alongWayWaterTem.setAirTem(Double.valueOf(result[3]));
            List<AlongWayWaterTem> alongWayWaterTems = dataStr.get(keys.size());
            if (org.springframework.util.CollectionUtils.isEmpty(alongWayWaterTems)) {
                alongWayWaterTems = org.apache.commons.compress.utils.Lists.newArrayList();
                dataStr.put(keys.size(), alongWayWaterTems);
            }
            alongWayWaterTems.add(alongWayWaterTem);
        } else {
            int size = keys.size();
            keys.add(size++);
        }
    }

    public PlanParamsResult getPlanParams(Integer planId, Integer planType) {
        PlanParamsResult planParamsResult = new PlanParamsResult();
        WaterTemperaturePlan waterTemperaturePlan;
        if (planId != null) {
            waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        } else {
            waterTemperaturePlan = waterTemperaturePlanMapper.selectListByPlanTypeOne(planType);
        }
        if (waterTemperaturePlan == null) {
            return planParamsResult;
        }
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(planId, 1);
        if (CollectionUtils.isNotEmpty(waterTemperaturePlanFiles)) {
            List<String> fileKeys = waterTemperaturePlanFiles.stream()
                    .map(WaterTemperaturePlanFile::getFileKey)
                    .collect(Collectors.toList());
            List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
            if (CollectionUtils.isNotEmpty(fileItemPOS)) {
                for (FileItemPO fileItemPO : fileItemPOS) {
                    switch (fileItemPO.getFileType()) {
                        case WA_SCHEMA_PARAM_WEATHER_RESERVOIR:
                            planParamsResult.setHdWeatherFileKey(fileItemPO.getFileKey());
                            planParamsResult.setHdWeatherFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW:
                            planParamsResult.setMainStreamInflowFileKey(fileItemPO.getFileKey());
                            planParamsResult.setMainStreamInflowFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE:
                            planParamsResult.setMainStreamTemperatureFileKey(fileItemPO.getFileKey());
                            planParamsResult.setMainStreamTemperatureFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW:
                            planParamsResult.setBranchStreamInflowFileKey(fileItemPO.getFileKey());
                            planParamsResult.setBranchStreamInflowFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE:
                            planParamsResult.setBranchStreamTemperatureFileKey(fileItemPO.getFileKey());
                            planParamsResult.setBranchStreamTemperatureFileName(fileItemPO.getFilename());
                            break;
                    }
                }
            }

        }
        planParamsResult.setPlanName(waterTemperaturePlan.getPlanName());
        planParamsResult.setPlanType(waterTemperaturePlan.getPlanType());
        planParamsResult.setInitialLevel(waterTemperaturePlan.getInitialLevel());
        planParamsResult.setPlanStartTime(waterTemperaturePlan.getPlanStartTime());
        planParamsResult.setPlanEndTime(waterTemperaturePlan.getPlanEndTime());
        planParamsResult.setMecovertcoe(waterTemperaturePlan.getMecovertcoe());
        planParamsResult.setSolarcoe(waterTemperaturePlan.getSolarcoe());
        planParamsResult.setWindcoe(waterTemperaturePlan.getWindcoe());
        planParamsResult.setInitialField(waterTemperaturePlan.getInitialField());
        return planParamsResult;
    }

    public PlanParamsResult obtainTaskParam(Integer planId, Integer planType) {
        PlanParamsResult planParamsResult = new PlanParamsResult();
        WaterTemperaturePlan waterTemperaturePlan;
        if (planId == null) {
            waterTemperaturePlan = waterTemperaturePlanMapper.selectListByPlanTypeOne(planType);
        } else {
            waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        }
        if (waterTemperaturePlan == null) {
            return planParamsResult;
        }
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(waterTemperaturePlan.getId(), 1);
        if (CollectionUtils.isNotEmpty(waterTemperaturePlanFiles)) {
            List<String> fileKeys = waterTemperaturePlanFiles.stream()
                    .map(WaterTemperaturePlanFile::getFileKey)
                    .collect(Collectors.toList());
            List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
            if (CollectionUtils.isNotEmpty(fileItemPOS)) {
                for (FileItemPO fileItemPO : fileItemPOS) {
                    switch (fileItemPO.getFileType()) {
                        case WA_SCHEMA_PARAM_WEATHER_RESERVOIR:
                            planParamsResult.setHdWeatherFileKey(fileItemPO.getFileKey());
                            planParamsResult.setHdWeatherFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW:
                            planParamsResult.setMainStreamInflowFileKey(fileItemPO.getFileKey());
                            planParamsResult.setMainStreamInflowFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE:
                            planParamsResult.setMainStreamTemperatureFileKey(fileItemPO.getFileKey());
                            planParamsResult.setMainStreamTemperatureFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW:
                            planParamsResult.setBranchStreamInflowFileKey(fileItemPO.getFileKey());
                            planParamsResult.setBranchStreamInflowFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE:
                            planParamsResult.setBranchStreamTemperatureFileKey(fileItemPO.getFileKey());
                            planParamsResult.setBranchStreamTemperatureFileName(fileItemPO.getFilename());
                            break;
                    }
                }
            }

        }
        planParamsResult.setPlanName(waterTemperaturePlan.getPlanName());
        planParamsResult.setPlanType(waterTemperaturePlan.getPlanType());
        planParamsResult.setPlanStartTime(waterTemperaturePlan.getPlanStartTime());
        planParamsResult.setPlanEndTime(waterTemperaturePlan.getPlanEndTime());
        planParamsResult.setWindcoe(waterTemperaturePlan.getWindcoe());
        planParamsResult.setMecovertcoe(waterTemperaturePlan.getMecovertcoe());
        planParamsResult.setSolarcoe(waterTemperaturePlan.getSolarcoe());
        return planParamsResult;
    }

    public RiverCalculateResult obtainCalculate(Integer planId){
        RiverCalculateResult riverCalculateResult = new RiverCalculateResult();
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        if (waterTemperaturePlan.getProgressStatus() != 6) {
            throw new ServiceException("任务执行未完成");
        }
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(planId, 2);
        if (CollectionUtils.isEmpty(waterTemperaturePlanFiles)) {
            return riverCalculateResult;
        }
        List<String> fileKeys = waterTemperaturePlanFiles.stream()
                .map(WaterTemperaturePlanFile::getFileKey)
                .collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        if (CollectionUtils.isEmpty(fileItemPOS)) {
            return riverCalculateResult;
        }
        CalculateViewVO calculateViewVO = new CalculateViewVO();
        List<String> ycswDiffUrl = Lists.newArrayList();
        List<String> calculateResultImage = Lists.newArrayList();
        List<String> differenceResultImage = Lists.newArrayList();

        List<String> profiles = Lists.newArrayList();
        List<String> alongs = Lists.newArrayList();
        for (FileItemPO fileItemPO : fileItemPOS) {
            UploadFileType fileType = fileItemPO.getFileType();
            switch (fileType) {
                case WA_SCHEMA_PARAM_RESER_OUT_GIF:
                    calculateViewVO.setYcswGifUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RESER_OUT_ALONG_WAY:
                    calculateViewVO.setDmswUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_PROFILE:
                    ycswDiffUrl.add(fileItemPO.getUrlPath());
                    //differenceResultImage.add(fileItemPO.getUrlPath());
                    profiles.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_ALONG_WAY:
                    calculateViewVO.setDmswDiffUrl(fileItemPO.getUrlPath());
                    //differenceResultImage.add(fileItemPO.getUrlPath());
                    alongs.add(fileItemPO.getUrlPath());
                    break;
            }
        }
        differenceResultImage.addAll(profiles);
        differenceResultImage.addAll(alongs);
        calculateViewVO.setYcswDiffUrl(ycswDiffUrl);
        riverCalculateResult.setDifferenceResultImage(differenceResultImage);
        riverCalculateResult.setCalculateResultImage(calculateResultImage);
        riverCalculateResult.setStartTime(waterTemperaturePlan.getCreateTime());
        riverCalculateResult.setEndTime(waterTemperaturePlan.getUpdateTime());
        riverCalculateResult.setViews(calculateViewVO);
        return riverCalculateResult;
    }

    private void setChartStyle(XYChart chart) {
        chart.getStyler().setChartBackgroundColor(new Color(12, 29, 56, 255)); // 使用透明色
        chart.getStyler().setPlotBackgroundColor(new Color(12, 29, 56, 255)); // 使用透明色
        chart.getStyler().setLegendBackgroundColor(new Color(12, 29, 56, 255)); // 使用透明色（可选）
        /*chart.getStyler().setChartBackgroundColor(XChatBackgroundColor);
        chart.getStyler().setLegendBackgroundColor(XChatBackgroundColor);*/

        //chart.getStyler().setYAxisLabelRotation.setYAxisGroupPosition(0, Styler.YAxisPosition.Left);

        chart.getStyler().setYAxisLabelAlignment(AxesChartStyler.TextAlignment.Centre);
        chart.getStyler().setLegendFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        chart.getStyler().setChartFontColor(Color.WHITE);
        chart.getStyler().setAxisTickLabelsColor(Color.WHITE);
        chart.getStyler().setAxisTickLabelsFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        chart.getStyler().setChartTitleFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        chart.getStyler().setAxisTitleFont(new Font("Microsoft YaHei", Font.BOLD, 22));
    }

    public void obtainDownLoadFile(WaterTemperaturePlan waterTemperaturePlan, Integer fileType, OutputStream outputStream) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(waterTemperaturePlan.getId(), fileType);
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        if (fileType == 2) {
            //输出结果文件
            for (FileItemPO fileItemPO : fileItemPOS) {
                String filePath = fileItemPO.getPath();
                log.info(fileItemPO.getFilename()+":{}", fileItemPO.getPath());
                ZipEntry zipEntry = new ZipEntry(fileItemPO.getFilename());
                Path path = Paths.get(filePath);
                if(Files.exists(path)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(Files.readAllBytes(path));
                    zipOutputStream.closeEntry();
                }
            }
            handleResidualExcel(zipOutputStream, waterTemperaturePlan);
        } else {
            //输出对比数据文件
            for (FileItemPO fileItemPO : fileItemPOS) {
                if (fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RESERVOIR_COMP)) {
                    String filePath = fileItemPO.getPath();
                    ZipEntry zipEntry = new ZipEntry(WA_SCHEMA_PARAM_RESERVOIR_COMP.getName() + fileItemPO.getExtension());
                    Path path = Paths.get(filePath);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(Files.readAllBytes(path));
                    zipOutputStream.closeEntry();
                }
            }
        }
        zipOutputStream.finish();
    }
}
