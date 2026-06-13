package com.jsj.datacenter.application.watertemperature.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.dto.river.*;
import com.jsj.datacenter.adapter.temp.SequenceWaterTem;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.application.watertemperature.WaterTemperatureReportsParams;
import com.jsj.datacenter.application.watertemperature.domain.WaterTempPlanFileItem;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlanFile;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperatureReports;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanFileMapper;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanMapper;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperatureReportsMapper;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.knowm.xchart.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.jsj.datacenter.infrastructure.common.enums.UploadFileType.*;

@Service
public class WaterTemperatureReportService {

    @Autowired
    WaterTemperatureReportsMapper waterTemperatureReportsMapper;

    @Autowired
    private WaterTemperaturePlanMapper waterTemperaturePlanMapper;

    @Autowired
    private FileItemMapper fileItemMapper;

    @Autowired
    private WaterTemperaturePlanFileMapper waterTemperaturePlanFileMapper;

    @Autowired
    private FileItemService fileItemService;

    public Page<WaterTemperatureReports> findWaterTemperatureReportsPage(WaterTemperatureReportsParams waterTemperatureReportsParams){
        int startNum = (waterTemperatureReportsParams.getPageNum() - 1) * waterTemperatureReportsParams.getPageSize();
        int i = waterTemperatureReportsMapper.selectListLikeNameCount(waterTemperatureReportsParams.getReportName());
        List<WaterTemperatureReports> waterTemperatureReports = waterTemperatureReportsMapper.selectListLikeNameLimitPage(waterTemperatureReportsParams.getReportName(), startNum, waterTemperatureReportsParams.getPageSize());
        Page<WaterTemperatureReports> waterTemperaturePage = new Page<>();
        waterTemperaturePage.setSize(waterTemperatureReportsParams.getPageSize());
        waterTemperaturePage.setRecords(waterTemperatureReports);
        waterTemperaturePage.setCurrent(waterTemperatureReportsParams.getPageNum());
        waterTemperaturePage.setTotal(i);
        return waterTemperaturePage;
    }

    public WaterTemperatureReports submitWaterTemperatureReports(WaterTemperatureReportVO waterTemperatureReportsVO){
        WaterTemperatureReports waterTemperatureReports = new WaterTemperatureReports();
        waterTemperatureReports.setReportName(waterTemperatureReportsVO.getReportName());
        String relatedTaskIds = waterTemperatureReportsVO.getRelatedTaskIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        waterTemperatureReports.setRelatedTaskIds(relatedTaskIds);
        waterTemperatureReports.setReportStatus(1);
        waterTemperatureReports.setReportCreateTime(new Date());
        waterTemperatureReports.setCreator("admin");
        waterTemperatureReportsMapper.insert(waterTemperatureReports);
        return waterTemperatureReports;
    }

    public WaterTemperatureReportResult getReportCountClassification() {
        WaterTemperatureReportResult waterTemperatureReportResult = new WaterTemperatureReportResult();
        waterTemperatureReportResult.setTotals(Math.toIntExact(waterTemperatureReportsMapper.selectCount(null)));
        waterTemperatureReportResult.setSaveNum(waterTemperatureReportsMapper.selectCountByReportStatus(1));
        waterTemperatureReportResult.setSubmitNum(waterTemperatureReportsMapper.selectCountByReportStatus(2));
        return waterTemperatureReportResult;
    }

    public WaterTemperatureReports getInfo(Long reportId){
        return waterTemperatureReportsMapper.selectById(reportId);
    }

    public void deleteReport(Long reportId) {
        waterTemperatureReportsMapper.deleteById(reportId);
    }

    public String reporting(Long reportId) {
        WaterTemperatureReports waterTemperatureReports = waterTemperatureReportsMapper.selectById(reportId);
        // 报告相关的所有任务id
        String relatedTaskIds = waterTemperatureReports.getRelatedTaskIds();
        List<Integer> taskIds = Arrays.stream(relatedTaskIds.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        assert CollectionUtils.isNotEmpty(taskIds);
        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMapper.selectBatchIds(taskIds);
        assert waterTemperaturePlans != null;
        // 根据方案类型分组，3-无挡墙水库 4-河道方案 5-有挡墙水库
        Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap = waterTemperaturePlans.stream()
                .collect(Collectors.groupingBy(WaterTemperaturePlan::getPlanType));
        Map<String, Object> dataTemplate = Maps.newHashMap();
        try {
            XWPFTemplate xwpfTemplate = this.buildXWPFTemplate();
            this.titleHandle(dataTemplate, waterTemperatureReports);
            this.paramHandle(dataTemplate, waterTemperaturePlanMap);
            this.schemeBaseInfoHandle(dataTemplate, waterTemperaturePlanMap);
            this.boundaryConditionHandle(dataTemplate, waterTemperaturePlanMap);
            this.verticalWaterTempHandle(dataTemplate, waterTemperaturePlanMap);
            this.dischargeWaterTempHandle(dataTemplate, waterTemperaturePlanMap, xwpfTemplate);
            this.riverWaterTemperatureComparisonHandle(dataTemplate, waterTemperaturePlanMap);
            this.layeredWaterDatasHandle(dataTemplate, waterTemperaturePlanMap);
            String path = this.render(xwpfTemplate, dataTemplate);
            waterTemperatureReports.setReportStatus(2);
            waterTemperatureReports.setReportUrl(path);
            waterTemperatureReportsMapper.updateById(waterTemperatureReports);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private XWPFTemplate buildXWPFTemplate() throws IOException {
        String rootDir = System.getProperty("user.dir");
        String docxTemplate = rootDir+"/script/template/旭龙水温分析报告模板.docx";

        LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy();
        Configure config = Configure.builder()
                .bind("reservoirs", policy)
                .bind("riverCourses", policy)
                .bind("schemeBaseInfos", policy)
                .bind("reservoirsCondition", policy)
                .bind("riverCoursesCondition", policy)
                .bind("verticalWaterTemperatures", policy)
                .bind("dischargeWaterConditions", policy)
                .bind("riverWaterTemperatureComparisons", policy)
                .bind("layeredWaterDatas", policy)
                .build();
        // 1. 编译模板
        return XWPFTemplate.compile(docxTemplate, config);


    }

    private String render(XWPFTemplate xwpfTemplate, Map<String, Object> dataTemplate) throws IOException {
        // 3. 渲染并输出
        String rootDir = System.getProperty("user.dir");
        String uuid = UUID.randomUUID().toString();
        xwpfTemplate.render(dataTemplate);
        String reportPath = rootDir+"/script/template/"+uuid+".docx";
        xwpfTemplate.writeToFile(reportPath);
        xwpfTemplate.close();
        Path path_out = Paths.get(reportPath);
        FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_WATER_TEMPERATURE_REPORT, "output.docx", Files.newInputStream(path_out));
        return fileItemDTO.getUrlPath();
    }

    // 生成表头信息
    private void titleHandle(Map<String, Object> data, WaterTemperatureReports waterTemperatureReports) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        data.put("timeStamp", LocalDate.now().format(formatter) + "-" +  System.currentTimeMillis());
        data.put("analysisTime", formatter.format(waterTemperatureReports.getReportCreateTime()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
        data.put("creator", waterTemperatureReports.getCreator());
    }

    private void paramHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) {
        // 水库方案，3无挡墙，5有挡墙
        List<WaterTemperaturePlan> wdReservoirPlans = waterTemperaturePlanMap.get(5);
        List<WaterTemperaturePlan> ydReservoirPlans = waterTemperaturePlanMap.get(3);

        //河道方案
        List<WaterTemperaturePlan> riverPlans = waterTemperaturePlanMap.get(4);

        List<WaterTemperaturePlan> reservoirPlans = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(wdReservoirPlans)) {
            reservoirPlans.addAll(wdReservoirPlans);
        }
        if (CollectionUtils.isNotEmpty(ydReservoirPlans)) {
            reservoirPlans.addAll(ydReservoirPlans);
        }
        List<WaterTemperaturePlan> waterTemperaturePlans = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(riverPlans)) {
            waterTemperaturePlans.addAll(riverPlans);
        }

        data.put("reservoirs", reservoirPlans);
        data.put("riverCourses", waterTemperaturePlans);
    }

    // 生成方案基础信息
    private void schemeBaseInfoHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) {
        // 水库方案，3无挡墙，5有挡墙
        List<WaterTemperaturePlan> wdReservoirPlans = waterTemperaturePlanMap.get(5);
        List<WaterTemperaturePlan> ydReservoirPlans = waterTemperaturePlanMap.get(3);

        //河道方案
        List<WaterTemperaturePlan> riverPlans = waterTemperaturePlanMap.get(4);
        List<SchemeBaseInfo> schemeBaseInfos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(wdReservoirPlans)) {
            schemeBaseInfos.addAll(wdReservoirPlans.stream().map(plan-> {
                SchemeBaseInfo schemeBaseInfo = new SchemeBaseInfo();
                schemeBaseInfo.setSchemeName(plan.getPlanName());
                schemeBaseInfo.setSchemeType("水库立面二维");
                schemeBaseInfo.setSchemeSubType("无前置挡墙");
                LocalDate localDate = plan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                schemeBaseInfo.setDateYear(localDate.getYear());
                return schemeBaseInfo;
            }).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(ydReservoirPlans)) {
            schemeBaseInfos.addAll(ydReservoirPlans.stream().map(plan-> {
                SchemeBaseInfo schemeBaseInfo = new SchemeBaseInfo();
                schemeBaseInfo.setSchemeName(plan.getPlanName());
                schemeBaseInfo.setSchemeType("水库里面二维");
                schemeBaseInfo.setSchemeSubType("有前置挡墙");
                LocalDate localDate = plan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                schemeBaseInfo.setDateYear(localDate.getYear());
                return schemeBaseInfo;
            }).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(riverPlans)) {
            schemeBaseInfos.addAll(riverPlans.stream().map(plan-> {
                SchemeBaseInfo schemeBaseInfo = new SchemeBaseInfo();
                schemeBaseInfo.setSchemeName(plan.getPlanName());
                schemeBaseInfo.setSchemeType("河道一维");
                schemeBaseInfo.setSchemeSubType("");
                LocalDate localDate = plan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                schemeBaseInfo.setDateYear(localDate.getYear());
                return schemeBaseInfo;
            }).collect(Collectors.toList()));
        }

        data.put("schemeBaseInfos", schemeBaseInfos);

    }

    // 生成边界条件参数
    private void boundaryConditionHandle(Map<String, Object> data,  Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) {
        this.reservoirBoundaryConditionHandle(data, waterTemperaturePlanMap.get(3), waterTemperaturePlanMap.get(5));
        this.riverBoundaryConditionHandle(data, waterTemperaturePlanMap.get(4));
    }

    // 生成水库边界条件
    private void reservoirBoundaryConditionHandle(Map<String, Object> data, List<WaterTemperaturePlan> ydPlans,  List<WaterTemperaturePlan> wdPlans) {
        WaterTemperaturePlan waterTemperaturePlan = ydPlans.get(0);
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileListByPlanId(waterTemperaturePlan.getId());
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        if (CollectionUtils.isEmpty(fileItemPOS)) {
            data.put("reservoirsCondition", Lists.newArrayList());
            return;
        }
        List<ReservoirBoundaryCondition> boundaryConditions = Lists.newArrayList();
        List<Map<Integer, String>> weathers = Collections.emptyList();
        List<Map<Integer, String>> inboundFlows = Collections.emptyList();
        List<Map<Integer, String>> outboundFlows = Collections.emptyList();
        List<Map<Integer, String>> inboundTemps = Collections.emptyList();
        for (FileItemPO fileItemPO : fileItemPOS) {
            UploadFileType fileType = fileItemPO.getFileType();
            switch (fileType) {
                case WA_SCHEMA_PARAM_WEATHER: //下泄水温
                     weathers = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                             .headRowNumber(2)
                            .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_INBOUND_TRAFFIC:  //输出excel
                    inboundFlows = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(4)
                            .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_OUTBOUND_TRAFFIC:  //二维云图 gif
                    outboundFlows = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(4)
                            .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_INBOUND_TEMP: //坝前温度
                    inboundTemps = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(4)
                            .doReadSync();
                    break;
            }
        }
        List<Map<Integer, String>> finalInboundFlows = inboundFlows;
        List<Map<Integer, String>> finalOutboundFlows = outboundFlows;
        List<Map<Integer, String>> finalInboundTemps = inboundTemps;
        List<ReservoirBoundaryConditionDay> reservoirBoundaryConditionDays = weathers.stream().map(item -> {
            ReservoirBoundaryConditionDay reservoirBoundaryConditionDay = new ReservoirBoundaryConditionDay();
            double dayDouble = Double.parseDouble(item.get(0));
            int day = (int) dayDouble;
            Double airTemperature = Double.parseDouble(item.get(1));
            Double inboundFlow = Double.parseDouble(finalInboundFlows.get(day-1).get(1));
            Double outboundFlow = Double.parseDouble(finalOutboundFlows.get(day-1).get(1));
            Double inboundTemp = Double.parseDouble(finalInboundTemps.get(day-1).get(1));
            reservoirBoundaryConditionDay.setDay(day);
            reservoirBoundaryConditionDay.setAirTemperature(airTemperature);
            reservoirBoundaryConditionDay.setInboundFlow(inboundFlow);
            reservoirBoundaryConditionDay.setOutboundFlow(outboundFlow);
            reservoirBoundaryConditionDay.setInboundTemperature(inboundTemp);
            return reservoirBoundaryConditionDay;
        }).collect(Collectors.toList());
        Map<Integer, List<ReservoirBoundaryConditionDay>> reservoirBoundaryConditionMap = reservoirBoundaryConditionDays.stream()
                .collect(Collectors.groupingBy(item -> LocalDate.ofYearDay(waterTemperaturePlan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear(), item.getDay())
                        .getMonthValue(), TreeMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<ReservoirBoundaryConditionDay>> integerListEntry : reservoirBoundaryConditionMap.entrySet()) {
            Integer month = integerListEntry.getKey();
            List<ReservoirBoundaryConditionDay> values = integerListEntry.getValue();
            ReservoirBoundaryCondition reservoirBoundaryCondition = new ReservoirBoundaryCondition();
            reservoirBoundaryCondition.setMonth(month);
            double inboundFlow = values.stream()
                    .mapToDouble(ReservoirBoundaryConditionDay::getInboundFlow)
                    .average()
                    .orElse(0.0);
            double outboundFlow = values.stream()
                    .mapToDouble(ReservoirBoundaryConditionDay::getOutboundFlow)
                    .average()
                    .orElse(0.0);
            double inboundTem = values.stream()
                    .mapToDouble(ReservoirBoundaryConditionDay::getInboundTemperature)
                    .average()
                    .orElse(0.0);
            double airTem = values.stream()
                    .mapToDouble(ReservoirBoundaryConditionDay::getAirTemperature)
                    .average()
                    .orElse(0.0);

            reservoirBoundaryCondition.setInboundFlow(this.bumRoundingHalfUp(inboundFlow));
            reservoirBoundaryCondition.setOutboundFlow(this.bumRoundingHalfUp(outboundFlow));
            reservoirBoundaryCondition.setInboundTemperature(this.bumRoundingHalfUp(inboundTem));
            reservoirBoundaryCondition.setAirTemperature(this.bumRoundingHalfUp(airTem));
            boundaryConditions.add(reservoirBoundaryCondition);
        }

        data.put("reservoirsCondition", boundaryConditions);
    }

    // 生成河道边界条件
    private void riverBoundaryConditionHandle(Map<String, Object> data, List<WaterTemperaturePlan> waterTemperaturePlans) {
        if (CollectionUtils.isEmpty(waterTemperaturePlans)) {
            data.put("riverCoursesCondition", Lists.newArrayList());
            return;
        }
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlans.get(0);
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileListByPlanId(waterTemperaturePlan.getId());
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        if (CollectionUtils.isEmpty(fileItemPOS)) {
            data.put("riverCoursesCondition", Lists.newArrayList());
            return;
        }
        List<Map<Integer, String>> weathers = Collections.emptyList();
        List<Map<Integer, String>> mainInboundFlows = Collections.emptyList();
        List<Map<Integer, String>> mainInboundTemps = Collections.emptyList();
        List<Map<Integer, String>> branchInboundFlows = Collections.emptyList();
        List<Map<Integer, String>> branchInboundTemps = Collections.emptyList();
        for (FileItemPO fileItemPO : fileItemPOS) {
            UploadFileType fileType = fileItemPO.getFileType();
            switch (fileType) {
                case WA_SCHEMA_PARAM_WEATHER_RESERVOIR: //下泄水温
                    weathers = EasyExcel.read(fileItemPO.getPath())
                        .sheet()
                        .headRowNumber(1)
                        .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW:  //输出excel
                    mainInboundFlows = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(2)
                            .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE:  //二维云图 gif
                    mainInboundTemps = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(1)
                            .doReadSync();
                    break;
                case WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW: //坝前温度
                    branchInboundFlows = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(1)
                            .doReadSync();
                    break;

                case WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE: //坝前温度
                    branchInboundTemps = EasyExcel.read(fileItemPO.getPath())
                            .sheet()
                            .headRowNumber(1)
                            .doReadSync();
                    break;
            }
        }
        List<Map<Integer, String>> finalMainInboundFlows = mainInboundFlows;
        List<Map<Integer, String>> finalMainInboundTemps = mainInboundTemps;
        List<Map<Integer, String>> finalBranchInboundFlows = branchInboundFlows;
        List<Map<Integer, String>> finalBranchInboundTemps = branchInboundTemps;
        List<RiverBoundaryConditionSeq> riverBoundaryConditionSeqs = weathers.stream().map(item -> {
            RiverBoundaryConditionSeq riverBoundaryConditionSeq = new RiverBoundaryConditionSeq();
            int day = Integer.parseInt(item.get(0));
            Double airTemperature = Double.parseDouble(item.get(2));
            Double mainInboundFlow = Double.parseDouble(finalMainInboundFlows.get(day-1).get(2));
            Double mainInboundTemp = Double.parseDouble(finalMainInboundTemps.get(day-1).get(1));
            Double branchInboundFlow = Double.parseDouble(finalBranchInboundFlows.get(day-1).get(2));
            Double branchInboundTemp = Double.parseDouble(finalBranchInboundTemps.get(day-1).get(1));
            riverBoundaryConditionSeq.setAirTemperature(airTemperature);
            riverBoundaryConditionSeq.setDischargedFlow(mainInboundFlow);
            riverBoundaryConditionSeq.setDischargedWaterTemperature(mainInboundTemp);
            riverBoundaryConditionSeq.setBranchFlow(branchInboundFlow);
            riverBoundaryConditionSeq.setBranchWaterTemperature(branchInboundTemp);
            return riverBoundaryConditionSeq;
        }).collect(Collectors.toList());
        List<List<RiverBoundaryConditionSeq>> partition = Lists.partition(riverBoundaryConditionSeqs, 6);
        List<RiverBoundaryConditionSeq> riverBoundaryConditionDays = Lists.newArrayList();
        for (int i = 0; i < partition.size(); i++) {
            RiverBoundaryConditionSeq riverBoundaryConditionSeq = new RiverBoundaryConditionSeq();
            riverBoundaryConditionSeq.setSeq(i+1);
            double airTem = partition.get(i).stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getAirTemperature)
                    .average()
                    .orElse(0.0);
            double disFlow = partition.get(i).stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getDischargedFlow)
                    .average()
                    .orElse(0.0);
            double disTem = partition.get(i).stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getDischargedWaterTemperature)
                    .average()
                    .orElse(0.0);
            double branchFlow = partition.get(i).stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getBranchFlow)
                    .average()
                    .orElse(0.0);
            double branchTem = partition.get(i).stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getBranchWaterTemperature)
                    .average()
                    .orElse(0.0);
            riverBoundaryConditionSeq.setAirTemperature(airTem);
            riverBoundaryConditionSeq.setDischargedFlow(disFlow);
            riverBoundaryConditionSeq.setDischargedWaterTemperature(disTem);
            riverBoundaryConditionSeq.setBranchFlow(branchFlow);
            riverBoundaryConditionSeq.setBranchWaterTemperature(branchTem);
            riverBoundaryConditionDays.add(riverBoundaryConditionSeq);
        }

        Map<Integer, List<RiverBoundaryConditionSeq>> riverBoundaryConditionSeqMap = riverBoundaryConditionDays.stream()
                .collect(Collectors.groupingBy(item -> LocalDate.ofYearDay(waterTemperaturePlan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear(), item.getSeq())
                        .getMonthValue(), TreeMap::new, Collectors.toList()));
        List<RiverBoundaryCondition> riverBoundaryConditions = Lists.newArrayList();
        for (Map.Entry<Integer, List<RiverBoundaryConditionSeq>> integerListEntry:riverBoundaryConditionSeqMap.entrySet()){
            RiverBoundaryCondition riverBoundaryCondition = new RiverBoundaryCondition();
            riverBoundaryCondition.setMonth(integerListEntry.getKey());
            List<RiverBoundaryConditionSeq> values = integerListEntry.getValue();
            double airTem =  values.stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getAirTemperature)
                    .average()
                    .orElse(0.0);
            double disFlow =  values.stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getDischargedFlow)
                    .average()
                    .orElse(0.0);
            double disTem =  values.stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getDischargedWaterTemperature)
                    .average()
                    .orElse(0.0);
            double branchFlow =  values.stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getBranchFlow)
                    .average()
                    .orElse(0.0);
            double branchTem =  values.stream()
                    .mapToDouble(RiverBoundaryConditionSeq::getBranchWaterTemperature)
                    .average()
                    .orElse(0.0);
            riverBoundaryCondition.setAirTemperature(this.bumRoundingHalfUp(airTem));
            riverBoundaryCondition.setDischargedFlow(this.bumRoundingHalfUp(disFlow));
            riverBoundaryCondition.setDischargedWaterTemperature(this.bumRoundingHalfUp(disTem));
            riverBoundaryCondition.setBranchFlow(this.bumRoundingHalfUp(branchFlow));
            riverBoundaryCondition.setBranchWaterTemperature(this.bumRoundingHalfUp(branchTem));
            riverBoundaryConditions.add(riverBoundaryCondition);
        }
        data.put("riverCoursesCondition", riverBoundaryConditions);
    }

    private void verticalWaterTempHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) throws IOException {
        data.put("verticalWaterTemperatures", Lists.newArrayList());
        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMap.get(3);
        if (CollectionUtils.isEmpty(waterTemperaturePlans)){
            return;
        }
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlans.get(0);
        data.put("ydPlanName", waterTemperaturePlan.getPlanName());

        WaterTempPlanFileItem waterTempPlanFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL");
        List<VerticalWaterTempCondition> verticalWaterTempConditions = EasyExcel.read(waterTempPlanFileItem.getPath())
                .sheet(2)
                .head(VerticalWaterTempCondition.class)
                .doReadSync();

        // 垂向水温天的数据
        /*List<VerticalWaterTempCondition> verticalWaterTempConditionsDay = EasyExcel.read(waterTempPlanFileItem.getPath())
                .sheet(3)
                .head(VerticalWaterTempCondition.class)
                .doReadSync();

        //
        long qe2Count = verticalWaterTempConditionsDay.stream().filter(condition -> condition.getVerticalTempDifference() > 2).count();
        long qe5Count = verticalWaterTempConditionsDay.stream().filter(condition -> condition.getVerticalTempDifference() > 5).count();
        long qe10Count = verticalWaterTempConditionsDay.stream().filter(condition -> condition.getVerticalTempDifference() > 10).count();
        data.put("qe2Count", qe2Count);
        data.put("qe5Count", qe5Count);
        data.put("qe10Count", qe10Count);*/


        for (int i = 0; i < verticalWaterTempConditions.size(); i++) {
            verticalWaterTempConditions.get(i).setMonth(i+1);
        }

        List<VerticalWaterTempCondition> verticalWaterTempConditionsList = verticalWaterTempConditions.stream().peek(item -> {
            item.setAgvWaterTemp(this.bumRoundingHalfUp(item.getAgvWaterTemp()));
            item.setSurfaceWaterTemp(this.bumRoundingHalfUp(item.getSurfaceWaterTemp()));
            item.setBottomWaterTemp(this.bumRoundingHalfUp(item.getBottomWaterTemp()));
            item.setVerticalTempDifference(this.bumRoundingHalfUp(item.getVerticalTempDifference()));
            item.setThickness(this.bumRoundingHalfUp(item.getThickness()==null?0.0000:item.getThickness()));
        }).collect(Collectors.toList());

        // 垂向水温数据
        data.put("verticalWaterTemperatures", verticalWaterTempConditionsList);
        // 垂向水温图片
        WaterTempPlanFileItem waterTempPlanCXSWFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RIVER_OUT_CXWD");
        data.put("waterTemperatureCXSWImage", Pictures.ofStream(Files.newInputStream(Paths.get(waterTempPlanCXSWFileItem.getPath())), PictureType.PNG)
                        .size(600, 450).center().create());

        // 坝前二位水温云图
        WaterTempPlanFileItem waterTempPlanYTWFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RIVER_OUT_BQWD");
        data.put("waterTemperatureYTImage", Pictures.ofStream(Files.newInputStream(Paths.get(waterTempPlanYTWFileItem.getPath())), PictureType.PNG)
                .size(600, 230).center().create());

        this.verticalWaterTempAnalyzeHandle(data, verticalWaterTempConditionsList);
    }

    private void verticalWaterTempAnalyzeHandle(Map<String, Object> data, List<VerticalWaterTempCondition> verticalWaterTempConditionsList) {
        VerticalWaterTempCondition verticalWaterTempConditionMin = this.calculationVerticalWaterTempMin(verticalWaterTempConditionsList);
        VerticalWaterTempCondition verticalWaterTempConditionMax = this.calculationVerticalWaterTempMax(verticalWaterTempConditionsList);
        VerticalWaterTempCondition verticalWaterTempConditionAgv = this.calculationVerticalWaterTempAgv(verticalWaterTempConditionsList);
        data.put("surfaceMin", verticalWaterTempConditionMin.getSurfaceWaterTemp());
        data.put("bottomMin", verticalWaterTempConditionMin.getBottomWaterTemp());
        data.put("verticalMin", verticalWaterTempConditionMin.getVerticalTempDifference());
        data.put("agvMin", verticalWaterTempConditionMin.getAgvWaterTemp());
        data.put("thicknessMin", verticalWaterTempConditionMin.getThickness());

        data.put("surfaceMax", verticalWaterTempConditionMax.getSurfaceWaterTemp());
        data.put("bottomMax", verticalWaterTempConditionMax.getBottomWaterTemp());
        data.put("verticalMax", verticalWaterTempConditionMax.getVerticalTempDifference());
        data.put("agvMax", verticalWaterTempConditionMax.getAgvWaterTemp());
        data.put("thicknessMax", verticalWaterTempConditionMax.getThickness());

        data.put("surfaceAgv", verticalWaterTempConditionAgv.getSurfaceWaterTemp());
        data.put("bottomAgv", verticalWaterTempConditionAgv.getBottomWaterTemp());
        data.put("verticalAgv", verticalWaterTempConditionAgv.getVerticalTempDifference());
        data.put("agvAgv", verticalWaterTempConditionAgv.getAgvWaterTemp());
        data.put("thicknessAgv", verticalWaterTempConditionAgv.getThickness());
    }

    private  VerticalWaterTempCondition calculationVerticalWaterTempAgv(List<VerticalWaterTempCondition> datas) {
        if (CollectionUtils.isEmpty(datas)){
            return null;
        }
        VerticalWaterTempCondition verticalWaterTempCondition = new VerticalWaterTempCondition();
        double surfaceAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getSurfaceWaterTemp)
                .average()
                .orElse(0.0);
        double bottomAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getBottomWaterTemp)
                .average()
                .orElse(0.0);
        double verticalAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getVerticalTempDifference)
                .average()
                .orElse(0.0);
        double agvAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getAgvWaterTemp)
                .average()
                .orElse(0.0);
        double thicknessAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getThickness)
                .average()
                .orElse(0.0);
        verticalWaterTempCondition.setSurfaceWaterTemp(this.bumRoundingHalfUp(surfaceAgv));
        verticalWaterTempCondition.setBottomWaterTemp(this.bumRoundingHalfUp(bottomAgv));
        verticalWaterTempCondition.setVerticalTempDifference(this.bumRoundingHalfUp(verticalAgv));
        verticalWaterTempCondition.setAgvWaterTemp(this.bumRoundingHalfUp(agvAgv));
        verticalWaterTempCondition.setThickness(this.bumRoundingHalfUp(thicknessAgv));
        return  verticalWaterTempCondition;
    }

    private  VerticalWaterTempCondition calculationVerticalWaterTempMax(List<VerticalWaterTempCondition> datas) {
        if (CollectionUtils.isEmpty(datas)){
            return null;
        }
        VerticalWaterTempCondition verticalWaterTempCondition = new VerticalWaterTempCondition();
        double surfaceAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getSurfaceWaterTemp)
                .max()
                .orElse(0.0);
        double bottomAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getBottomWaterTemp)
                .max()
                .orElse(0.0);
        double verticalAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getVerticalTempDifference)
                .max()
                .orElse(0.0);
        double agvAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getAgvWaterTemp)
                .max()
                .orElse(0.0);
        double thicknessAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getThickness)
                .max()
                .orElse(0.0);
        verticalWaterTempCondition.setSurfaceWaterTemp(surfaceAgv);
        verticalWaterTempCondition.setBottomWaterTemp(bottomAgv);
        verticalWaterTempCondition.setVerticalTempDifference(verticalAgv);
        verticalWaterTempCondition.setAgvWaterTemp(agvAgv);
        verticalWaterTempCondition.setThickness(thicknessAgv);
        return  verticalWaterTempCondition;
    }

    private  VerticalWaterTempCondition calculationVerticalWaterTempMin(List<VerticalWaterTempCondition> datas) {
        if (CollectionUtils.isEmpty(datas)){
            return null;
        }
        VerticalWaterTempCondition verticalWaterTempCondition = new VerticalWaterTempCondition();
        double surfaceAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getSurfaceWaterTemp)
                .min()
                .orElse(0.0);
        double bottomAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getBottomWaterTemp)
                .min()
                .orElse(0.0);
        double verticalAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getVerticalTempDifference)
                .min()
                .orElse(0.0);
        double agvAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getAgvWaterTemp)
                .min()
                .orElse(0.0);
        double thicknessAgv = datas
                .stream()
                .mapToDouble(VerticalWaterTempCondition::getThickness)
                .min()
                .orElse(0.0);
        verticalWaterTempCondition.setSurfaceWaterTemp(surfaceAgv);
        verticalWaterTempCondition.setBottomWaterTemp(bottomAgv);
        verticalWaterTempCondition.setVerticalTempDifference(verticalAgv);
        verticalWaterTempCondition.setAgvWaterTemp(agvAgv);
        verticalWaterTempCondition.setThickness(thicknessAgv);
        return  verticalWaterTempCondition;
    }

    /**
     * 下泄水温数据处理器
     * @param data  渲染参数
     * @param waterTemperaturePlanMap 水温方案Map,根据类型分类
     * @param xWPFTemplate  docx文档模板类
     */
    private void dischargeWaterTempHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap, XWPFTemplate xWPFTemplate){
        data.put("dischargeWaterConditions", Lists.newArrayList());
        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMap.get(3);
        if (CollectionUtils.isEmpty(waterTemperaturePlans)){
            return;
        }
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlans.get(0);
        WaterTempPlanFileItem waterTempPlanXXWFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL");
        String path = waterTempPlanXXWFileItem.getPath();
        List<DischargeWaterCondition> dischargeWaterConditions = EasyExcel.read(path)
                .sheet()
                .head(DischargeWaterCondition.class)
                .doReadSync();

        // data渲染数据， path下泄温水数据
        this.dischargeWaterTempDayHandle(data, path, waterTemperaturePlan);

        // 获取1.3.1 表格第四行 自然水温数据
        XWPFTableRow row  = xWPFTemplate.getXWPFDocument().getAllTables().get(0).getRow(3);
        XWPFTableRow rowBzl  = xWPFTemplate.getXWPFDocument().getAllTables().get(0).getRow(4);//奔子栏
        for (int i = 0; i < dischargeWaterConditions.size(); i++) {
            Double naturalWaterTemp = Double.valueOf(row.getCell(i + 2).getText());
            Double naturalS35Temp = Double.valueOf(rowBzl.getCell(i + 2).getText());
            double diffValue = this.bumRoundingHalfUp(dischargeWaterConditions.get(i).getDischargeWaterTemp() - naturalWaterTemp);
            dischargeWaterConditions.get(i).setNaturalWaterTemp(this.bumRoundingHalfUp(naturalWaterTemp));
            dischargeWaterConditions.get(i).setDischargeWaterTemp(this.bumRoundingHalfUp(dischargeWaterConditions.get(i).getDischargeWaterTemp()));
            dischargeWaterConditions.get(i).setDifference(diffValue);
            dischargeWaterConditions.get(i).setNaturalS35Temp(naturalS35Temp);
        }
        data.put("dischargeWaterConditions", dischargeWaterConditions);

        // 下降水温
        List<DischargeWaterCondition> dischargeDowns = dischargeWaterConditions.stream().filter(item -> item.getDifference() < 0).collect(Collectors.toList());
        // 下降的月份
        String downMonthStr = StringUtils.join(dischargeDowns.stream().map(DischargeWaterCondition::getMonth).toArray(), "月,");
        double downAvg = dischargeDowns.stream().mapToDouble(DischargeWaterCondition::getDifference).average().orElse(0.0);
        DischargeWaterCondition downMaxData = dischargeDowns.stream()
                .reduce(dischargeDowns.get(0), (a, b) ->
                        a.getDifference().compareTo(b.getDifference()) < 0 ? a : b);
        data.put("downMonthStr", downMonthStr);
        data.put("downAvg", this.bumRoundingHalfUp(downAvg));
        data.put("downMaxData", downMaxData.getMonth()+"月");
        data.put("downMax", downMaxData.getDifference());

        // 上升温度
        List<DischargeWaterCondition> dischargeUps = dischargeWaterConditions.stream().filter(item -> item.getDifference() >= 0).collect(Collectors.toList());
        // 上升的月份
        String upMonthStr = StringUtils.join(dischargeUps.stream().map(DischargeWaterCondition::getMonth).toArray(), "月,");
        double upAvg = dischargeUps.stream().mapToDouble(DischargeWaterCondition::getDifference).average().orElse(0.0);
        DischargeWaterCondition upMaxData = dischargeDowns.stream()
                .reduce(dischargeDowns.get(0), (a, b) ->
                        a.getDifference().compareTo(b.getDifference()) > 0 ? a : b);
        data.put("upMonthStr", upMonthStr);
        data.put("upAvg", this.bumRoundingHalfUp(upAvg));
        data.put("upMaxData", upMaxData.getMonth()+"月");
        data.put("upMax", upMaxData.getDifference());



        // 下泄水温图
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(230)
                .title("下泄水温")
                .xAxisTitle("时间（月）")
                .yAxisTitle("水温（° C）")
                .build();
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(false);
        List<Double> naturals = dischargeWaterConditions.stream().map(DischargeWaterCondition::getNaturalWaterTemp).collect(Collectors.toList());
        List<Double> discharges = dischargeWaterConditions.stream().map(DischargeWaterCondition::getDischargeWaterTemp).collect(Collectors.toList());
        chart.addSeries("天然水温", naturals);
        chart.addSeries("下泄水温", discharges);
        data.put("waterTemperatureXXImage", Pictures.ofBufferedImage(BitmapEncoder.getBufferedImage(chart), PictureType.PNG)
                .size(600, 230).center().create());

        List<Double> naturalDatas = dischargeWaterConditions.stream().map(DischargeWaterCondition::getNaturalWaterTemp).collect(Collectors.toList());
        List<Double> dischargeDatas = dischargeWaterConditions.stream().map(DischargeWaterCondition::getDischargeWaterTemp).collect(Collectors.toList());
        List<Double> differenceDatas = dischargeWaterConditions.stream().map(DischargeWaterCondition::getDifference).collect(Collectors.toList());
        Double naturalAgv = this.calculationAgv(naturalDatas);
        Double naturalMax = this.calculationMax(naturalDatas);
        Double naturalMin = this.calculationMin(naturalDatas);
        Double naturalRange = naturalMax - naturalMin;

        Double dischargeAgv = this.calculationAgv(dischargeDatas);
        Double dischargeMax = this.calculationMax(dischargeDatas);
        Double dischargeMin = this.calculationMin(dischargeDatas);
        Double dischargeRange = dischargeMax - dischargeMin;

        Double differenceAge = this.calculationAgv(differenceDatas);
        Double differenceMax = this.calculationMax(differenceDatas);
        Double differenceMin = this.calculationMin(differenceDatas);
        Double differenceRange = differenceMax - differenceMin;
        data.put("naturalAgv", naturalAgv);
        data.put("naturalMax", naturalMax);
        data.put("naturalMin", naturalMin);
        data.put("naturalRange", naturalRange);

        data.put("dischargeAgv", dischargeAgv);
        data.put("dischargeMax", dischargeMax);
        data.put("dischargeMin", dischargeMin);
        data.put("dischargeRange", dischargeRange);

        data.put("differenceAge", differenceAge);
        data.put("differenceMax", differenceMax);
        data.put("differenceMin", differenceMin);
        data.put("differenceRange", differenceRange);
    }

    private void dischargeWaterTempDayHandle(Map<String, Object> data, String path, WaterTemperaturePlan waterTemperaturePlan) {
        String rootDir = System.getProperty("user.dir");
        List<DischargeWaterConditionDay> dischargeWaterConditionsDays = EasyExcel.read(path)
               .sheet(1)
               .head(DischargeWaterConditionDay.class)
               .doReadSync();
        //天然垂向水温
        List<DischargeNaturalWaterConditionDay> dischargeNaturalWaterConditionDays = EasyExcel.read(rootDir + "/script/template/natural.xlsx")
                .sheet(0)
                .head(DischargeNaturalWaterConditionDay.class)
                .doReadSync();

        int naturalDay = 0;
        List<DischargeNaturalWaterConditionDay> dischargeNaturalWaterConditionDayList = dischargeNaturalWaterConditionDays.stream().filter(item -> item.getDamsiteTemperature() > 12).collect(Collectors.toList());
        if(dischargeNaturalWaterConditionDayList.size() >=3 ){
            for (int i = 0; i < dischargeNaturalWaterConditionDayList.size()-2; i++) {
                Integer day = dischargeNaturalWaterConditionDayList.get(i).getDay();
                Integer day1 = dischargeNaturalWaterConditionDayList.get(i + 1).getDay();
                Integer day2 = dischargeNaturalWaterConditionDayList.get(i + 2).getDay();
                if (day == day1-1 && day == day2-2) {
                    naturalDay = day;
                }
            }
        }
        int disDay = 0;
        List<DischargeWaterConditionDay> dischargeWaterConditionDays = dischargeWaterConditionsDays.stream().filter(item -> item.getTemperature() > 12).collect(Collectors.toList());
        if(dischargeWaterConditionDays.size() >=3 ){
            for (int i = 0; i < dischargeWaterConditionDays.size()-2; i++) {
               Integer day = dischargeWaterConditionDays.get(i).getDay();
               Integer day1 = dischargeWaterConditionDays.get(i + 1).getDay();
               Integer day2 = dischargeWaterConditionDays.get(i + 2).getDay();
               if (day == day1-1 && day == day2-2) {
                   disDay = day;
               }
            }
        }
        LocalDate localDate = LocalDate.ofYearDay(waterTemperaturePlan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear(), disDay);
        String dateFormat = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        data.put("dischargeRiseDate", dateFormat);
        data.put("dischargeRiseDay", disDay-naturalDay);

    }

    private void riverWaterTemperatureComparisonHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) {
        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMap.get(4);
        if (CollectionUtils.isEmpty(waterTemperaturePlans)){
            data.put("riverWaterTemperatureComparisons", Lists.newArrayList());
        }
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlans.get(0);
        data.put("riverPlanName", waterTemperaturePlan.getPlanName());

        WaterTempPlanFileItem waterTempPlanFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RESER_OUT_RESULT_EXCEL");
        List<SequenceWaterTem> sequenceWaterComTems = EasyExcel.read(waterTempPlanFileItem.getPath())
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();
        sequenceWaterComTems.remove(0);
        Map<Integer, List<SequenceWaterTem>> sequenceWaterTemMap = sequenceWaterComTems.stream()
                .collect(Collectors.groupingBy(item -> LocalDate.ofYearDay(waterTemperaturePlan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear(), item.getDay().intValue())
                        .getMonthValue(), TreeMap::new, Collectors.toList()));

        List<RiverWaterTemperatureComparison> riverWaterTemperatureComparisons = Lists.newArrayList();
        Set<Map.Entry<Integer, List<SequenceWaterTem>>> entries = sequenceWaterTemMap.entrySet();

        List<DischargeWaterCondition> dischargeWaterConditions = (List<DischargeWaterCondition>) data.get("dischargeWaterConditions");
        for (Map.Entry<Integer, List<SequenceWaterTem>> entry : entries) {
            RiverWaterTemperatureComparison riverWaterTemperatureComparison = new RiverWaterTemperatureComparison();
            riverWaterTemperatureComparison.setMonth(entry.getKey());
            double s35Temp = entry.getValue().stream()
                    .mapToDouble(SequenceWaterTem::getS2)
                    .average()
                    .orElse(0.0);
            riverWaterTemperatureComparison.setRiverS35Temp(this.bumRoundingHalfUp(s35Temp));
            if (CollectionUtils.isNotEmpty(dischargeWaterConditions)) {
                DischargeWaterCondition dischargeWaterCondition = dischargeWaterConditions.get(entry.getKey() - 1);
                riverWaterTemperatureComparison.setRiverDamSiteTemp(this.bumRoundingHalfUp(dischargeWaterCondition.getDischargeWaterTemp()));
                riverWaterTemperatureComparison.setNaturalDamSiteTemp(this.bumRoundingHalfUp(dischargeWaterCondition.getNaturalWaterTemp()));
                riverWaterTemperatureComparison.setNaturalS35Temp(this.bumRoundingHalfUp(dischargeWaterCondition.getNaturalS35Temp()));
            }
            riverWaterTemperatureComparison.setRiverS35TempDiff(this.bumRoundingHalfUp(riverWaterTemperatureComparison.getRiverS35Temp()-riverWaterTemperatureComparison.getNaturalS35Temp()));
            riverWaterTemperatureComparison.setRiverDamSiteTempDiff(this.bumRoundingHalfUp(riverWaterTemperatureComparison.getRiverDamSiteTemp()- riverWaterTemperatureComparison.getNaturalDamSiteTemp()));

            riverWaterTemperatureComparison.setAlongNaturalTempDiff(this.bumRoundingHalfUp(riverWaterTemperatureComparison.getNaturalS35Temp()-riverWaterTemperatureComparison.getNaturalDamSiteTemp()));
            riverWaterTemperatureComparison.setAlongRiverTempDiff(this.bumRoundingHalfUp(riverWaterTemperatureComparison.getRiverS35Temp()- riverWaterTemperatureComparison.getRiverDamSiteTemp()));
            riverWaterTemperatureComparison.setTempDiff(this.bumRoundingHalfUp(riverWaterTemperatureComparison.getAlongRiverTempDiff()- riverWaterTemperatureComparison.getAlongNaturalTempDiff()));
            riverWaterTemperatureComparisons.add(riverWaterTemperatureComparison);
        }
        data.put("riverWaterTemperatureComparisons", riverWaterTemperatureComparisons);



        List<Double> naturalDamSiteDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getNaturalDamSiteTemp).collect(Collectors.toList());
        List<Double> naturalS35Datas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getNaturalS35Temp).collect(Collectors.toList());
        List<Double> riverDamSiteDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getRiverDamSiteTemp).collect(Collectors.toList());
        List<Double> riverS35Datas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getRiverS35Temp).collect(Collectors.toList());
        List<Double> riverDamSiteDiffDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getRiverDamSiteTempDiff).collect(Collectors.toList());
        List<Double> riverS3DiffDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getRiverS35TempDiff).collect(Collectors.toList());
        List<Double> alongNaturalDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getAlongNaturalTempDiff).collect(Collectors.toList());
        List<Double> alongRiverDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getAlongRiverTempDiff).collect(Collectors.toList());
        List<Double> tempDiffDatas = riverWaterTemperatureComparisons.stream().map(RiverWaterTemperatureComparison::getTempDiff).collect(Collectors.toList());

        Double naturalAgv = this.calculationAgv(naturalDamSiteDatas);
        Double naturalMax = this.calculationMax(naturalDamSiteDatas);
        Double naturalMin = this.calculationMin(naturalDamSiteDatas);
        Double naturalRange = naturalMax - naturalMin;
        data.put("naturalAgv", this.bumRoundingHalfUp(naturalAgv));
        data.put("naturalMax", this.bumRoundingHalfUp(naturalMax));
        data.put("naturalMin", this.bumRoundingHalfUp(naturalMin));
        data.put("naturalRange", this.bumRoundingHalfUp(naturalRange));
        Double naturalS35Agv = this.calculationAgv(naturalS35Datas);
        Double naturalS35Max = this.calculationMax(naturalS35Datas);
        Double naturalS35Min = this.calculationMin(naturalS35Datas);
        Double naturalS35Range = naturalS35Max - naturalS35Min;
        data.put("naturalS35Agv", this.bumRoundingHalfUp(naturalS35Agv));
        data.put("naturalS35Max", this.bumRoundingHalfUp(naturalS35Max));
        data.put("naturalS35Min", this.bumRoundingHalfUp(naturalS35Min));
        data.put("naturalS35Range", this.bumRoundingHalfUp(naturalS35Range));
        Double riverDamSiteAgv = this.calculationAgv(riverDamSiteDatas);
        Double riverDamSiteMax = this.calculationMax(riverDamSiteDatas);
        Double riverDamSiteMin = this.calculationMin(riverDamSiteDatas);
        Double riverDamSiteRange = riverDamSiteMax - riverDamSiteMin;
        data.put("riverDamSiteAgv", this.bumRoundingHalfUp(riverDamSiteAgv));
        data.put("riverDamSiteMax", this.bumRoundingHalfUp(riverDamSiteMax));
        data.put("riverDamSiteMin", this.bumRoundingHalfUp(riverDamSiteMin));
        data.put("riverDamSiteRange", this.bumRoundingHalfUp(riverDamSiteRange));
        Double riverS35Agv = this.calculationAgv(riverS35Datas);
        Double riverS35Max = this.calculationMax(riverS35Datas);
        Double riverS35Min = this.calculationMin(riverS35Datas);
        Double riverS35Range = riverS35Max - riverS35Min;
        data.put("riverS35Agv", this.bumRoundingHalfUp(riverS35Agv));
        data.put("riverS35Max", this.bumRoundingHalfUp(riverS35Max));
        data.put("riverS35Min", this.bumRoundingHalfUp(riverS35Min));
        data.put("riverS35Range", this.bumRoundingHalfUp(riverS35Range));
        Double riverDamSiteDiffAgv = this.calculationAgv(riverDamSiteDiffDatas);
        Double riverDamSiteDiffMax = this.calculationMax(riverDamSiteDiffDatas);
        Double riverDamSiteDiffMin = this.calculationMin(riverDamSiteDiffDatas);
        Double riverDamSiteDiffRange = riverDamSiteMax - riverDamSiteMin;
        data.put("riverDamSiteAgv", this.bumRoundingHalfUp(riverDamSiteDiffAgv));
        data.put("riverDamSiteMax", this.bumRoundingHalfUp(riverDamSiteDiffMax));
        data.put("riverDamSiteMin", this.bumRoundingHalfUp(riverDamSiteDiffMin));
        data.put("riverDamSiteRange", this.bumRoundingHalfUp(riverDamSiteDiffRange));
        Double riverS3DiffAgv = this.calculationAgv(riverS3DiffDatas);
        Double riverS3DiffMax = this.calculationMax(riverS3DiffDatas);
        Double riverS3DiffMin = this.calculationMin(riverS3DiffDatas);
        Double riverS3DiffRange = riverS3DiffMax - riverS3DiffMin;
        data.put("riverS3DiffAgv", this.bumRoundingHalfUp(riverS3DiffAgv));
        data.put("riverS3DiffMax", this.bumRoundingHalfUp(riverS3DiffMax));
        data.put("riverS3DiffMin", this.bumRoundingHalfUp(riverS3DiffMin));
        data.put("riverS3DiffRange", this.bumRoundingHalfUp(riverS3DiffRange));
        Double alongNaturaAgv = this.calculationAgv(alongNaturalDatas);
        Double alongNaturaMax = this.calculationMax(alongNaturalDatas);
        Double alongNaturaMin = this.calculationMin(alongNaturalDatas);
        Double alongNaturaRange = alongNaturaMax - alongNaturaMin;
        data.put("alongNaturaAgv", this.bumRoundingHalfUp(alongNaturaAgv));
        data.put("alongNaturaMax", this.bumRoundingHalfUp(alongNaturaMax));
        data.put("alongNaturaMin", this.bumRoundingHalfUp(alongNaturaMin));
        data.put("alongNaturaRange", this.bumRoundingHalfUp(alongNaturaRange));
        Double alongRiverAgv = this.calculationAgv(alongRiverDatas);
        Double alongRiverMax = this.calculationMax(alongRiverDatas);
        Double alongRiverMin = this.calculationMin(alongRiverDatas);
        Double alongRiverRange = alongRiverMax - alongRiverMin;
        data.put("alongRiverAgv", this.bumRoundingHalfUp(alongRiverAgv));
        data.put("alongRiverMax", this.bumRoundingHalfUp(alongRiverMax));
        data.put("alongRiverMin", this.bumRoundingHalfUp(alongRiverMin));
        data.put("alongRiverRange", this.bumRoundingHalfUp(alongRiverRange));
        Double tempDiffAgv = this.calculationAgv(tempDiffDatas);
        Double tempDiffMax = this.calculationMax(tempDiffDatas);
        Double tempDiffMin = this.calculationMin(tempDiffDatas);
        Double tempDiffRange = tempDiffMax - tempDiffMin;
        data.put("tempDiffAgv", this.bumRoundingHalfUp(tempDiffAgv));
        data.put("tempDiffMax", this.bumRoundingHalfUp(tempDiffMax));
        data.put("tempDiffMin", this.bumRoundingHalfUp(tempDiffMin));
        data.put("tempDiffRange", this.bumRoundingHalfUp(tempDiffRange));

        String diffMonth = StringUtils.join(riverWaterTemperatureComparisons.stream()
                .filter(item->item.getTempDiff()>0.3)
                .map(RiverWaterTemperatureComparison::getMonth).toArray(), "月,");
        data.put("diffMonth", diffMonth);

        List<RiverWaterTemperatureComparison> riverUp = riverWaterTemperatureComparisons.stream()
                .filter(item -> item.getRiverS35TempDiff() > 0)
                .collect(Collectors.toList());
        double maxUp = riverUp.stream().mapToDouble(RiverWaterTemperatureComparison::getRiverS35Temp).max().orElse(0.0);
        double avgUp = riverUp.stream().mapToDouble(RiverWaterTemperatureComparison::getRiverS35Temp).average().orElse(0.0);
        String upMonth = StringUtils.join(riverUp.stream().map(RiverWaterTemperatureComparison::getMonth).toArray(), "月,");
        data.put("maxUp", this.bumRoundingHalfUp(maxUp));
        data.put("avgUp", this.bumRoundingHalfUp(avgUp));
        data.put("upMonth", upMonth);
        List<RiverWaterTemperatureComparison> riverDown = riverWaterTemperatureComparisons.stream()
                .filter(item->item.getRiverS35TempDiff() < 0)
                .collect(Collectors.toList());
        double maxDown = riverDown.stream().mapToDouble(RiverWaterTemperatureComparison::getRiverS35Temp).min().orElse(0.0);
        double avgDown = riverDown.stream().mapToDouble(RiverWaterTemperatureComparison::getRiverS35Temp).average().orElse(0.0);
        String downMonth = StringUtils.join(riverDown.stream().map(RiverWaterTemperatureComparison::getMonth).toArray(), "月,");
        data.put("maxDown", this.bumRoundingHalfUp(Math.abs(maxDown)));
        data.put("avgDown", this.bumRoundingHalfUp(avgDown));
        data.put("downMonth", downMonth);

        String rootDir = System.getProperty("user.dir");
        //天然奔子栏
        List<DischargeNaturalWaterConditionDay> dischargeNaturalWaterConditionDays = EasyExcel.read(rootDir + "/script/template/natural.xlsx")
                .sheet(0)
                .head(DischargeNaturalWaterConditionDay.class)
                .doReadSync();
        List<DischargeNaturalWaterConditionDay> upDayNaturals = dischargeNaturalWaterConditionDays.stream()
                .filter(item -> item.getS35Temperature() > 12)
                .collect(Collectors.toList());
        int naturalDay = 0;
        if(upDayNaturals.size() >=3 ){
            for (int i = 0; i < upDayNaturals.size()-2; i++) {
                Integer day = upDayNaturals.get(i).getDay();
                Integer day1 = upDayNaturals.get(i + 1).getDay();
                Integer day2 = upDayNaturals.get(i + 2).getDay();
                if (day == day1-1 && day == day2-2) {
                    naturalDay = day;
                }
            }
        }
        int disDay = 0;
        List<SequenceWaterTem> upDayRivers = sequenceWaterComTems.stream().filter(item -> item.getS2() > 12).collect(Collectors.toList());
        if(upDayRivers.size() >=3 ){
            for (int i = 0; i < upDayRivers.size()-2; i++) {
                int day = Math.toIntExact(Math.round(upDayRivers.get(i).getDay()));
                int day1 = Math.toIntExact(Math.round(upDayRivers.get(i + 1).getDay()));
                int day2 = Math.toIntExact(Math.round(upDayRivers.get(i + 2).getDay()));
                if (day == day1-1 && day == day2-2) {
                    disDay = day;
                }
            }
        }
        LocalDate localDate = LocalDate.ofYearDay(waterTemperaturePlan.getPlanStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear(), disDay);
        String dateFormat = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        data.put("riverRiseDate", dateFormat);
        data.put("riverRiseDay", disDay-naturalDay);
        /*
        //按天的奔子栏
        for(SequenceWaterTem sequenceWaterTem:sequenceWaterComTems){
            DischargeNaturalWaterConditionDay dischargeNaturalWaterConditionDay = dischargeNaturalWaterConditionDays.get((int) (sequenceWaterTem.getDay() - 1));
            dischargeNaturalWaterConditionDay.setDamsiteTemperature(sequenceWaterTem.getS2());
        }*/

    }

    private void layeredWaterDatasHandle(Map<String, Object> data, Map<Integer, List<WaterTemperaturePlan>> waterTemperaturePlanMap) {
        data.put("layeredWaterDatas", Lists.newArrayList());
        // 有挡下泄水温
        List<DischargeWaterCondition> dischargeWaterConditions = (List<DischargeWaterCondition>) data.get("dischargeWaterConditions");
        List<LayeredWaterData> layeredWaterDatas = dischargeWaterConditions.stream().map(item -> {
            LayeredWaterData layeredWaterData = new LayeredWaterData();
            layeredWaterData.setMonth(item.getMonth());
            layeredWaterData.setNaturalWaterTemp(item.getNaturalWaterTemp());
            layeredWaterData.setYdWaterTemp(item.getDischargeWaterTemp());
            return layeredWaterData;
        }).collect(Collectors.toList());

        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMap.get(5);
        if (CollectionUtils.isNotEmpty(waterTemperaturePlans)){
            WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlans.get(0);
            WaterTempPlanFileItem waterTempPlanXXWFileItem = waterTemperaturePlanMapper.selectWaterTempPlanFileItemByIdAndFileType(waterTemperaturePlan.getId(), "WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL");
            String path = waterTempPlanXXWFileItem.getPath();
            List<DischargeWaterCondition> dischargeWaterConditionWds = EasyExcel.read(path)
                    .sheet()
                    .head(DischargeWaterCondition.class)
                    .doReadSync();
            layeredWaterDatas.forEach(item -> {
                DischargeWaterCondition dischargeWaterCondition = dischargeWaterConditionWds.get(item.getMonth() - 1);
                if (dischargeWaterCondition != null) {
                    Double dischargeWaterTemp = dischargeWaterCondition.getDischargeWaterTemp()==null?0:dischargeWaterCondition.getDischargeWaterTemp();
                    item.setWdWaterTemp(this.bumRoundingHalfUp(dischargeWaterTemp));
                }
                item.setDifference(item.getYdWaterTemp()-(item.getWdWaterTemp()==null?0.0: item.getWdWaterTemp()));
            });

        }
        // 筛选4-7月份
        layeredWaterDatas = layeredWaterDatas.stream().filter(item -> item.getMonth()>3 && item.getMonth()<8).collect(Collectors.toList());
        data.put("layeredWaterDatas", layeredWaterDatas);
        LayeredWaterData layeredWaterMaxData = layeredWaterDatas.stream()
                .max(Comparator.comparingDouble(LayeredWaterData::getDifference)).orElse(null);
        if (layeredWaterMaxData != null){
            data.put("layeredWaterMoth", layeredWaterMaxData.getMonth());
            data.put("layeredWaterMothDiff", layeredWaterMaxData.getDifference());
            data.put("layeredWaterAvg", layeredWaterDatas.stream().mapToDouble(LayeredWaterData::getYdWaterTemp).average().orElse(0.0));
        }
        // 折线图
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(230)
                .title("")
                .xAxisTitle("月份")
                .yAxisTitle("水温（° C）")
                .build();
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(false);
        List<Double> naturals = layeredWaterDatas.stream().map(LayeredWaterData::getNaturalWaterTemp).collect(Collectors.toList());
        List<Double> ydTemps = layeredWaterDatas.stream().map(LayeredWaterData::getYdWaterTemp).collect(Collectors.toList());
        List<Double> wdTemps = layeredWaterDatas.stream().map(LayeredWaterData::getWdWaterTemp).collect(Collectors.toList());
        chart.addSeries("坝址天然", naturals);
        chart.addSeries("有前置挡墙", ydTemps);
        chart.addSeries("无前置挡墙", wdTemps);
        data.put("layeredWaterZxImage", Pictures.ofBufferedImage(BitmapEncoder.getBufferedImage(chart), PictureType.PNG)
                .size(600, 230).center().create());


        // 柱状图
        CategoryChart categoryChart = new CategoryChartBuilder()
                .width(600)
                .height(230)
                .title("")
                .xAxisTitle("月份")
                .yAxisTitle("水温（° C）")
                .build();
        categoryChart.getStyler().setChartBackgroundColor(Color.WHITE);
        categoryChart.getStyler().setPlotGridLinesVisible(false);
        List<String> xDatas = layeredWaterDatas.stream().map(item -> item.getMonth() + "月份").collect(Collectors.toList());
        List<Double> naturalTempsY =  layeredWaterDatas.stream()
                .map(LayeredWaterData::getNaturalWaterTemp)
                .collect(Collectors.toList());

        List<Double> ydTempsY =  layeredWaterDatas.stream()
                .map(LayeredWaterData::getYdWaterTemp)
                .collect(Collectors.toList());

        List<Double> wdTempsY =  layeredWaterDatas.stream()
                .map(LayeredWaterData::getWdWaterTemp)
                .collect(Collectors.toList());
        categoryChart.addSeries("坝址天然", xDatas, naturalTempsY);
        categoryChart.addSeries("有前置挡墙", xDatas, ydTempsY);
        categoryChart.addSeries("无前置挡墙", xDatas, wdTempsY);
        data.put("layeredWaterCategoryImage", Pictures.ofBufferedImage(BitmapEncoder.getBufferedImage(categoryChart), PictureType.PNG)
                .size(600, 230).center().create());

    }

    private Double calculationAgv(List<Double> datas){
        return this.bumRoundingHalfUp(datas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private Double calculationMax(List<Double> datas){
        return datas.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private Double calculationMin(List<Double> datas){
        return datas.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private double  bumRoundingHalfUp(Double num){
        return BigDecimal.valueOf(num).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
