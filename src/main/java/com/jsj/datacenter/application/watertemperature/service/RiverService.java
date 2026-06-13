package com.jsj.datacenter.application.watertemperature.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.adapter.temp.DischargeWaterResidualTemperature;
import com.jsj.datacenter.adapter.temp.DischargeWaterTemperature;
import com.jsj.datacenter.adapter.temp.VerticalWaterResidualTemp;
import com.jsj.datacenter.adapter.temp.VerticalWaterTemperature;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlanFile;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanFileMapper;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanMapper;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.application.watertemperature.result.RiverExecuteLog;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import com.jsj.datacenter.infrastructure.vo.CalculateViewVO;
import com.jsj.datacenter.screen.ReservoirPaintbrush;
import com.jsj.datacenter.util.StringUtils;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.jsj.datacenter.infrastructure.common.enums.UploadFileType.*;

@Service
@Slf4j
public class RiverService {

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private FileItemMapper fileItemMapper;

    @Autowired
    private Executor threadPoolTaskExecutor;

    @Autowired
    private WaterTemperaturePlanMapper waterTemperaturePlanMapper;

    @Autowired
    private WaterTemperaturePlanFileMapper waterTemperaturePlanFileMapper;

    @Autowired
    private ReservoirPaintbrush reservoirPaintbrush;

    public WaterTemperaturePlan executePy(RiverCalculateVO riverCalculate) throws IOException, InterruptedException {
        if (riverCalculate.getPlanType() == null){
            riverCalculate.setPlanType(1);
        }
        WaterTemperaturePlan waterTemperaturePlan = null;
        if (riverCalculate.getPlan_id() != null) {
            waterTemperaturePlan = waterTemperaturePlanMapper.selectById(riverCalculate.getPlan_id());
            waterTemperaturePlan.setTotalDay(riverCalculate.getTotalDay());
            waterTemperaturePlan.setPlanType(riverCalculate.getPlanType());
            waterTemperaturePlan.setExh(riverCalculate.getExh2O());
            waterTemperaturePlan.setSpread(riverCalculate.getSpread());
            waterTemperaturePlan.setInitialLevel(riverCalculate.getInitialLevel());
            waterTemperaturePlan.setBeta(riverCalculate.getBeta());
            waterTemperaturePlan.setPlanStartTime(riverCalculate.getPlanStartTime());
            waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
            waterTemperaturePlan.setPlanEndTime(riverCalculate.getPlanEndTime());
            waterTemperaturePlan.setUpdateTime(new Date());
        } else {
            if (riverCalculate.getPlanType().equals(1)) {
                waterTemperaturePlan = waterTemperaturePlanMapper.selectListByPlanTypeOne(1);
            }
            if(waterTemperaturePlan == null){
                waterTemperaturePlan = new WaterTemperaturePlan();
                waterTemperaturePlan.setPlanName(riverCalculate.getPlanName());
                waterTemperaturePlan.setPlanType(riverCalculate.getPlanType());
                waterTemperaturePlan.setCreateTime(new Date());
                waterTemperaturePlan.setUpdateTime(new Date());
                waterTemperaturePlan.setExh(riverCalculate.getExh2O());
                waterTemperaturePlan.setSpread(riverCalculate.getSpread());
                waterTemperaturePlan.setInitialLevel(riverCalculate.getInitialLevel());
                waterTemperaturePlan.setBeta(riverCalculate.getBeta());
                waterTemperaturePlan.setPlanStartTime(riverCalculate.getPlanStartTime());
                waterTemperaturePlan.setPlanEndTime(riverCalculate.getPlanEndTime());
                waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
                waterTemperaturePlanMapper.insert(waterTemperaturePlan);
            }
            waterTemperaturePlan.setCreateTime(new Date());
            waterTemperaturePlan.setUpdateTime(new Date());
        }
        waterTemperaturePlan.setPlanStartTime(riverCalculate.getPlanStartTime());
        waterTemperaturePlan.setPlanEndTime(riverCalculate.getPlanEndTime());
        waterTemperaturePlan.setTotalDay(riverCalculate.getTotalDay());
        waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
        waterTemperaturePlan.setProgressStatus(1);
        waterTemperaturePlan.setProcess("任务开始");
        waterTemperaturePlanMapper.updateById(waterTemperaturePlan);
        WaterTemperaturePlan finalWaterTemperaturePlan = waterTemperaturePlan;
        this.executeScript(riverCalculate, finalWaterTemperaturePlan);
        return  waterTemperaturePlan;
    }

    public void executeScript(RiverCalculateVO riverCalculate, WaterTemperaturePlan finalWaterTemperaturePlan){
        saveBoundaryConditions(riverCalculate, finalWaterTemperaturePlan);
        threadPoolTaskExecutor.execute(() -> {
            try {
                String rootDir = System.getProperty("user.dir");
                String inputFilePath;
                if (finalWaterTemperaturePlan.getPlanType() == 1 || finalWaterTemperaturePlan.getPlanType() == 3){
                    inputFilePath = rootDir + "/script/xl/XL-weir";
                } else {
                    inputFilePath = rootDir + "/script/xl/XL-weir-wdq";
                }
                waterTemperaturePlanFileMapper.deleteByPlanIdAndType(finalWaterTemperaturePlan.getId(), 2);
                RiverCalculateResult riverCalculateResult = new RiverCalculateResult();
                riverCalculateResult.setViews(new CalculateViewVO());
                this.handleInputFile(riverCalculate, inputFilePath);
                this.waitingScript(finalWaterTemperaturePlan.getPlanType(), finalWaterTemperaturePlan.getTotalDay());
                //下泄水温脚本开始执行,,待修改
                finalWaterTemperaturePlan.setProgressStatus(2);
                finalWaterTemperaturePlan.setProcess("下泄水温脚本开始执行");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
                executeXXScript(finalWaterTemperaturePlan.getId(), riverCalculate, inputFilePath);
                //二维云图绘制脚本开始执行
                finalWaterTemperaturePlan.setProgressStatus(3);
                finalWaterTemperaturePlan.setProcess("二维云图绘制脚本开始执行");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
                executeYTScript(finalWaterTemperaturePlan.getId(), inputFilePath);
                //坝前温度随时间变化二维云图脚本开始执行
                finalWaterTemperaturePlan.setProgressStatus(4);
                finalWaterTemperaturePlan.setProcess("坝前温度随时间变化二维云图脚本开始执行");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
                executeBQSWScript(finalWaterTemperaturePlan.getId(), inputFilePath);
                //垂直温度结构脚本开始执行，， 待修改
                finalWaterTemperaturePlan.setProgressStatus(5);
                finalWaterTemperaturePlan.setProcess("垂直温度结构脚本开始执行");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
                executeCXWDScript(finalWaterTemperaturePlan.getId(), riverCalculate, inputFilePath);
                //任务执行完成
                finalWaterTemperaturePlan.setProgressStatus(6);
                finalWaterTemperaturePlan.setProcess("任务执行完成");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);

                riverCalculateResult.setDiscrepancy(Lists.newArrayList());
            } catch (Exception e) {
                //执行任务异常
                e.printStackTrace();
                log.error(e.getMessage());
                finalWaterTemperaturePlan.setProgressStatus(-3);
                finalWaterTemperaturePlan.setProcess("执行任务异常");
                waterTemperaturePlanMapper.updateById(finalWaterTemperaturePlan);
            }
        });
    }

    private void waitingScript(Integer planType, Integer process) {
        for (;;) {
            try {
                Double v = this.readFile(planType);
                Double v1 = Double.valueOf(process);
                if (v.equals(v1)) {
                    if (planType == 1 || planType == 3) {
                        Runtime.getRuntime().exec("taskkill /f /im w2_v4_64_yd.exe");
                        return;
                    } else {
                        Runtime.getRuntime().exec("taskkill /f /im w2_v4_64_wd.exe");
                        return;
                    }
                }
                Thread.sleep(60000);
            } catch (InterruptedException | IOException ignored) {

            }
        }
    }

    private void saveBoundaryConditions(RiverCalculateVO riverCalculate, WaterTemperaturePlan waterTemperaturePlan) {
        waterTemperaturePlanFileMapper.deleteByPlanIdAndType(waterTemperaturePlan.getId(), 1);
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(riverCalculate.getWeatherFileKey());
        waterTemperaturePlanFile.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFile.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);

        WaterTemperaturePlanFile waterTemperaturePlanFileInboundTraffic = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileInboundTraffic.setFileKey(riverCalculate.getInboundTrafficFileKey());
        waterTemperaturePlanFileInboundTraffic.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileInboundTraffic.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileInboundTraffic);

        WaterTemperaturePlanFile waterTemperaturePlanFileOutboundTraffic = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileOutboundTraffic.setFileKey(riverCalculate.getOutboundTrafficFileKey());
        waterTemperaturePlanFileOutboundTraffic.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileOutboundTraffic.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileOutboundTraffic);

        WaterTemperaturePlanFile waterTemperaturePlanFileInboundTemp = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileInboundTemp.setFileKey(riverCalculate.getInboundTempFileKey());
        waterTemperaturePlanFileInboundTemp.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileInboundTemp.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileInboundTemp);

        WaterTemperaturePlanFile waterTemperaturePlanFileComparison = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileComparison.setFileKey(riverCalculate.getComparisonFileKey());
        waterTemperaturePlanFileComparison.setPlanId(waterTemperaturePlan.getId());
        waterTemperaturePlanFileComparison.setFileType(1);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileComparison);
    }

    /**
     * 根据任务计划id获取任务日志
     * @param planId 任务id
     * @return
     */
    public RiverExecuteLog queryTaskLog(Integer planId) throws IOException {
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        RiverExecuteLog riverExecuteLog = new RiverExecuteLog();
        riverExecuteLog.setCreateTime(waterTemperaturePlan.getCreateTime().getTime());
        riverExecuteLog.setPlanType(waterTemperaturePlan.getPlanType());
        riverExecuteLog.setPlanName(waterTemperaturePlan.getPlanName());
        riverExecuteLog.setPlanId(waterTemperaturePlan.getId());
        riverExecuteLog.setTotalDays(waterTemperaturePlan.getTotalDay());
        if ( waterTemperaturePlan.getProgressStatus() != 6 && waterTemperaturePlan.getProgressStatus() > 3 ) {
            Integer planType = waterTemperaturePlan.getPlanType();
            riverExecuteLog.setProgressStatus(this.readFile(planType).intValue());
        } else if (waterTemperaturePlan.getProgressStatus() == 6) {
            riverExecuteLog.setProgressStatus(-2);
        } else if (waterTemperaturePlan.getProgressStatus() == -3){
            riverExecuteLog.setProgressStatus(waterTemperaturePlan.getProgressStatus());
        } else {
            riverExecuteLog.setProgressStatus(0);
        }
        log.info("queryTaskLog:{}", riverExecuteLog.getProgressStatus());
        return riverExecuteLog;
    }

    private Double readFile(Integer planType) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String regex = ",\\s+|\\s+";
        String logItem;
        if (planType == 1 || planType == 3 || planType ==5) {
            String baseSkDir = rootDir + "/script/xl/XL-weir/two_str1_seg130.opt";
            List<String> fileLogs = Files.readAllLines(Paths.get(baseSkDir));
            logItem = fileLogs.get(fileLogs.size() - 1);

        } else {
            String baseDir = rootDir + "/script/ricen/Output/Time Sequence/Ice/twtm.dat";
            List<String> fileLogs = Files.readAllLines(Paths.get(baseDir));
            logItem = fileLogs.get(fileLogs.size() - 1);
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(logItem)) {
            return 0.00;
        }
        return Double.parseDouble(logItem.trim().split(regex)[0]);
    }

    /**
     * 根据任务id获取计算结果
     * @param planId 任务id
     * @return
     */
    public RiverCalculateResult obtainCalculate(Integer planId){
        RiverCalculateResult riverCalculateResult = new RiverCalculateResult();
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        if (waterTemperaturePlan.getProgressStatus() != 6) {
            throw new ServiceException("任务执行未完成");
        }
        riverCalculateResult.setStartTime(waterTemperaturePlan.getCreateTime());
        riverCalculateResult.setEndTime(waterTemperaturePlan.getUpdateTime());
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
        List<String> calculateResultImage = Lists.newArrayList();
        List<String> differenceResultImage = Lists.newArrayList();
        for (FileItemPO fileItemPO : fileItemPOS) {
            UploadFileType fileType = fileItemPO.getFileType();
            switch (fileType) {
                case WA_SCHEMA_PARAM_RIVER_OUT_XX: //下泄水温
                    calculateViewVO.setXxswUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL:  //输出excel
                    riverCalculateResult.setResultExcelUrl(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_YT:  //二维云图 gif
                    calculateViewVO.setGifUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_BQWD: //坝前温度
                    calculateViewVO.setBqcxswUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_CXWD:  //锤向温度
                    calculateViewVO.setCxwdUrl(fileItemPO.getUrlPath());
                    calculateResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_XX_DIFF: //下泄温度
                    calculateViewVO.setXxswDiffUrl(fileItemPO.getUrlPath());
                    differenceResultImage.add(fileItemPO.getUrlPath());
                    break;
                case WA_SCHEMA_PARAM_RIVER_OUT_CXWD_DIFF:  //锤向温度
                    calculateViewVO.setCxwdDiffUrl(fileItemPO.getUrlPath());
                    differenceResultImage.add(fileItemPO.getUrlPath());
                    break;
            }
        }
        riverCalculateResult.setViews(calculateViewVO);
        riverCalculateResult.setCalculateResultImage(calculateResultImage);
        riverCalculateResult.setDifferenceResultImage(differenceResultImage);
        return riverCalculateResult;
    }

    /**
     * 处理上传excel文件参数，转化为python执行入参
     */
    public void handleInputFile(RiverCalculateVO riverCalculate, String inputFilePath) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String baseDir =inputFilePath;
        FileItemPO fileItemWeather = fileItemMapper.getFile(riverCalculate.getWeatherFileKey());
        FileItemPO fileInboundTemp = fileItemMapper.getFile(riverCalculate.getInboundTempFileKey());
        FileItemPO fileInboundTraffic = fileItemMapper.getFile(riverCalculate.getInboundTrafficFileKey());
        FileItemPO fileOutboundTraffic = fileItemMapper.getFile(riverCalculate.getOutboundTrafficFileKey());
        Double fileInitialLevel = riverCalculate.getInitialLevel()==null? 0: riverCalculate.getInitialLevel();
        List<String> fLWSBlock = generateELWSBlock(fileInitialLevel);
       /* List<String> readAllLines = Files.readAllLines(Paths.get(baseDir + "/bth.npt"));
        int elwsIndex = readAllLines.indexOf("ELWS");
        readAllLines.addAll(elwsIndex, fLWSBlock);
        Files.write(Paths.get(baseDir + "/bth.npt"), readAllLines);*/
        String file_dir_met_out = baseDir + "/met.npt";
        excelToMet(fileItemWeather.getPath(), file_dir_met_out);
        excelToQin(fileInboundTraffic.getPath(), baseDir + "/qin_br1.csv");
        excelToQin(fileOutboundTraffic.getPath(), baseDir+"/qot_br1.csv");
        excelToQin(fileInboundTemp.getPath(), baseDir+"/tin_br1.csv");
        /*Runtime runtime = Runtime.getRuntime();;
        runtime.exec( rootDir+"/script/xl/XL-weir/w2_v4_64.exe");*/

        ProcessBuilder pb = new ProcessBuilder("wscript", baseDir+"/invis.vbs");
        File vbsFile = new File(baseDir+"/invis.vbs");
        pb.redirectErrorStream(true); // 合并错误流和输出流
        pb.directory(vbsFile.getParentFile());
        try {
            Process process = pb.start();
            process.waitFor();
            // 读取输出（同上）
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<String> generateELWSBlock(Double fileInitialLevel) {
        List<String> lines = Lists.newArrayList();
        for (int i = 0; i < 13; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                stringBuilder.append(StringUtils.alignLeft(String.valueOf(fileInitialLevel), 12));
            }
            lines.add(stringBuilder.toString());
        }
        lines.add(String.valueOf(fileInitialLevel));
        return lines;
    }

    /**
     * 执行 下泄水温绘制.py脚本方法
     * @throws IOException
     * @throws InterruptedException
     */
    public void executeXXScript(Integer planId, RiverCalculateVO riverCalculate, String inputFilePath) throws IOException, InterruptedException {
        //获取项链路径
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/下泄水温绘制.py";
        String outPath = rootDir+"/uploads/river/xx/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }

        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                inputFilePath+"/two_130.opt",
                rootDir+"/uploads/river/xx/"+startTime+".png",
                rootDir+"/uploads/river/xx/"+startTime+".xlsx");
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束
        process.waitFor();
        FileItemDTO fileItemDTOPNG = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_XX, new File(rootDir + "/uploads/river/xx/" + startTime + ".png"));
        FileItemDTO fileItemDTOEXCEL = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL, new File(rootDir + "/uploads/river/xx/" + startTime + ".xlsx"));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTOPNG.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);

        WaterTemperaturePlanFile waterTemperaturePlanFileEXCEL = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileEXCEL.setFileKey(fileItemDTOEXCEL.getFileKey());
        waterTemperaturePlanFileEXCEL.setFileType(2);
        waterTemperaturePlanFileEXCEL.setPlanId(planId);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileEXCEL);

        // 生成对比数据
        //获取对比数据
        FileItemPO comparisonFile = fileItemMapper.getFile(riverCalculate.getComparisonFileKey());
        FileItemPO excelFile = fileItemMapper.getFile(fileItemDTOEXCEL.getFileKey());
        if (comparisonFile != null) {
            List<DischargeWaterTemperature> dischargeWaterTemperaturesDiff = reservoirPaintbrush.analysisDischargeWaterTemperature(comparisonFile.getPath(),1);
            List<DischargeWaterTemperature> dischargeWaterTemperatures = reservoirPaintbrush.analysisDischargeWaterTemperature(excelFile.getPath(), 0);
            FileItemDTO fileItemDiffImgDTO = reservoirPaintbrush.paintbrushXXWT(dischargeWaterTemperatures, dischargeWaterTemperaturesDiff);
            WaterTemperaturePlanFile waterTemperaturePlanFileDiffImg = new WaterTemperaturePlanFile();
            waterTemperaturePlanFileDiffImg.setFileKey(fileItemDiffImgDTO.getFileKey());
            waterTemperaturePlanFileDiffImg.setPlanId(planId);
            waterTemperaturePlanFileDiffImg.setFileType(2);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileDiffImg);
        }

    }

    /**
     * 执行 二维云图绘制.py脚本方法
     * @throws IOException
     * @throws InterruptedException
     */
    public String executeYTScript(Integer planId, String inputFilePath) throws IOException, InterruptedException {
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/二维云图绘制.py";
        String outPath = rootDir+"/uploads/river/yt/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                inputFilePath+"/cpl.opt",
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束
        process.waitFor();
        return pngToGif(planId);
    }

    /**
     * 坝前温度随时间变化二维云图(prf).py 脚本执行
     * @throws IOException
     * @throws InterruptedException
     */
    public String executeBQSWScript(Integer planId, String inputFilePath) throws IOException, InterruptedException {
        String bth_file = "/bth.npt";
        String elev_file = "/elevation.opt";
        String prf_file = "/prf.opt";
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/坝前温度随时间变化二维云图(prf).py";
        String outPath = rootDir+"/uploads/river/bqsw/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                inputFilePath + bth_file,
                inputFilePath + elev_file,
                inputFilePath + prf_file,
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束
        process.waitFor();
        FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_BQWD, new File(outPath + "prf.png"));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
        return outPath+"prf.png";
    }

    /**
     * 垂向温度结构结果整理输出.py 脚本执行
     * @throws IOException
     * @throws InterruptedException
     */
    public String executeCXWDScript(Integer planId, RiverCalculateVO riverCalculate, String inputFilePath) throws IOException, InterruptedException {
        String rootDir = System.getProperty("user.dir");
        String bth_file = "/snp.opt";
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/垂向温度结构结果整理输出.py";
        String outPath = rootDir+"/uploads/river/cxwd/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                inputFilePath + bth_file,
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束 //垂向水温结构.xlsx
        process.waitFor();
        FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_CXWD, new File(outPath + "垂向水温结构.png"));
        FileItemDTO fileItemExcelDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL, new File(outPath + "垂向水温结构.xlsx"));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);

        WaterTemperaturePlanFile waterTemperaturePlanFileExcel = new WaterTemperaturePlanFile();
        waterTemperaturePlanFileExcel.setFileKey(fileItemExcelDTO.getFileKey());
        waterTemperaturePlanFileExcel.setPlanId(planId);
        waterTemperaturePlanFileExcel.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileExcel);

        //获取对比数据
        FileItemPO comparisonFile = fileItemMapper.getFile(riverCalculate.getComparisonFileKey());
        FileItemPO excelFile = fileItemMapper.getFile(fileItemExcelDTO.getFileKey());
        if (comparisonFile != null) {
            List<VerticalWaterTemperature> verticalWaterTemperaturesDiff = reservoirPaintbrush.analysisVerticalWaterTemperatureFile(comparisonFile.getPath(),0);
            List<VerticalWaterTemperature> verticalWaterTemperatures = reservoirPaintbrush.analysisVerticalWaterTemperatureFile(excelFile.getPath(), 0);
            FileItemDTO fileItemDiffImgDTO = reservoirPaintbrush.paintbrushHorizontalDiffWT(verticalWaterTemperatures, verticalWaterTemperaturesDiff);
            WaterTemperaturePlanFile waterTemperaturePlanFileDiffImg = new WaterTemperaturePlanFile();
            waterTemperaturePlanFileDiffImg.setFileKey(fileItemDiffImgDTO.getFileKey());
            waterTemperaturePlanFileDiffImg.setPlanId(planId);
            waterTemperaturePlanFileDiffImg.setFileType(2);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFileDiffImg);
        }
        return outPath+"垂向水温结构.png";
    }

    /**
     * png图片转gif动画
     * @throws IOException
     */
    private String pngToGif(Integer planId) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir = rootDir+"/uploads/river/yt/";
        AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
        //生成的图片路径
        animatedGifEncoder.start(file_dir+"/reservoir.gif");
        File file = new File(file_dir+"/reservoir.gif");
        if (file.exists()){
            file.delete();
        }
        //图片之间间隔时间
        animatedGifEncoder.setDelay(3000);
        //重复次数 0表示无限重复 默认不重复
        animatedGifEncoder.setRepeat(0);
        animatedGifEncoder.setQuality(10);
        animatedGifEncoder.setBackground(new java.awt.Color(12, 29, 56, 255));
        animatedGifEncoder.setTransparent(new java.awt.Color(12, 29, 56, 255));
        File folder = new File(file_dir);
        // 获取文件夹下的所有文件
        File[] files = folder.listFiles();
        assert files != null;
        Arrays.sort(files, Comparator.comparing(item-> {
            String name = item.getName();
            String nameWithoutExt = name.substring(0, name.lastIndexOf('.'));
            // 按分隔符分割
            String[] segments = nameWithoutExt.split("_");
            // 获取第i段（从0开始计数）
            if (segments.length > 1) {
                String iSegment = segments[1]; // 获取第二段
                return Integer.parseInt(iSegment);
            }
            return 255;
        }));
        //WA_SCHEMA_PARAM_RIVER_OUT_DIMENSIONAL_YT
        for (int i = 0; i < files.length-1; i++) {
            FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_DIMENSIONAL_YT, i+".png" , new FileInputStream(files[i]));
            WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
            waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
            waterTemperaturePlanFile.setPlanId(planId);
            waterTemperaturePlanFile.setFileType(2);
            waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
            BufferedImage read = ImageIO.read(files[i]);
            // 设置生成图片大小
            animatedGifEncoder.setSize(read.getWidth(), read.getHeight());
            animatedGifEncoder.addFrame(read);
        }
        FileItemDTO fileItemDTO = fileItemService.saveFileData(WA_SCHEMA_PARAM_RIVER_OUT_YT, new File(file_dir + "/reservoir.gif"));
        WaterTemperaturePlanFile waterTemperaturePlanFile = new WaterTemperaturePlanFile();
        waterTemperaturePlanFile.setFileKey(fileItemDTO.getFileKey());
        waterTemperaturePlanFile.setPlanId(planId);
        waterTemperaturePlanFile.setFileType(2);
        waterTemperaturePlanFileMapper.insert(waterTemperaturePlanFile);
        return file_dir+"/reservoir.gif";
    }



    public int excelParse(MultipartFile excelFile, UploadFileType fileType) {
        try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream())) {
            int dayNum = 0;
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum() + 1;//总行数
            System.out.println("文件总行数："+lastRowNum);
            switch (fileType) {
                case WA_SCHEMA_PARAM_WEATHER:
                    dayNum = lastRowNum-2;
                    break;
                case WA_SCHEMA_PARAM_INBOUND_TRAFFIC:
                    dayNum = lastRowNum-4;
                    break;
                case WA_SCHEMA_PARAM_OUTBOUND_TRAFFIC:
                    dayNum = lastRowNum-4;
                    break;
                case WA_SCHEMA_PARAM_INBOUND_TEMP:
                    dayNum = lastRowNum-4;
                    break;
                case WA_SCHEMA_PARAM_INITIAL_LEVEL:
                    dayNum = lastRowNum;
                    break;
                case WA_SCHEMA_PARAM_RIVER_COMP:
                    dayNum = lastRowNum;
                    break;
                /**
                 * WA_SCHEMA_PARAM_WEATHER_RESERVOIR("气象参数", "excel", WaSchemaWeatherItemVO.class),
                 *     WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW("干流流量参数", "excel", WaSchemaInTrafficItemVO.class),
                 *     WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE("干流温度参数", "excel", WaSchemaOutTrafficItemVO.class),
                 *     WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW("支流流量参数", "excel", WaSchemaOutTrafficItemVO.class),
                 *     WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE("支流温度参数", "excel", WaSchemaOutTrafficItemVO.class),
                 *     WA_SCHEMA_PARAM_RIVER_COMP("对比结果数据", "excel", WaSchemaOutTrafficItemVO.class),
                 */
                //河道模型边界条件
                case WA_SCHEMA_PARAM_WEATHER_RESERVOIR:
                    dayNum = (lastRowNum-1)/6;
                    break;
                case WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW:
                    dayNum = (lastRowNum-2)/6;
                    break;
                case WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE:
                    dayNum = (lastRowNum-1)/6;
                    break;
                case WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW:
                    dayNum = (lastRowNum-1)/6;
                    break;
                case WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE:
                    dayNum = (lastRowNum-1)/6;
                    break;
                //对比数据
                case WA_SCHEMA_PARAM_RESERVOIR_COMP:
                    dayNum = lastRowNum-5;
                    break;

            }
            return dayNum;
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }
    }

    private void excelToMet(String excelPath, String file_dir_out) throws IOException {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File excelFile = new File(excelPath);
            File out_file = new File(file_dir_out);
            if (out_file.exists()) {
                out_file.mkdir();
            }
            fw = new FileWriter(out_file);
            bw = new BufferedWriter(fw);
            try (Workbook workbook = WorkbookFactory.create(new FileInputStream(excelFile))) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    StringBuilder stringBuffer = new StringBuilder();
                    if (row.getRowNum() == 0) {
                        bw.write(row.getCell(0).getStringCellValue());
                        bw.newLine();
                        continue;
                    }
                    if (row.getRowNum() == 1) {
                        continue;
                    }
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.NUMERIC) {
                            stringBuffer.append(StringUtils.alignRight(String.valueOf(cell.getNumericCellValue()), 8));
                        } else {
                            stringBuffer.append(StringUtils.alignRight(cell.getStringCellValue(), 8));
                        }
                    }
                    bw.write(stringBuffer.toString());
                    bw.newLine();
                    System.out.println(stringBuffer);
                }
            }
            bw.flush();
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }

    }


    private void excelToQin(String excelPath, String file_dir_out) throws IOException {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File excelFile = new File(excelPath);
            String rootDir = System.getProperty("user.dir");
            //String file_dir = rootDir + "/input/river/meteorology.xlsx";
            //String file_dir_out = rootDir + "/input/river/met.npt";
            // 创建FileWriter和BufferedWriter来写入文件
            File out_file = new File(file_dir_out);
            if (out_file.exists()) {
                out_file.mkdir();
            }
            fw = new FileWriter(out_file);
            bw = new BufferedWriter(fw);
            try (Workbook workbook = WorkbookFactory.create(new FileInputStream(excelFile))) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    StringBuffer stringBuffer = new StringBuffer();
                    if (row.getRowNum() == 3) {
                        continue;
                    }
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.NUMERIC) {
                            stringBuffer.append(cell.getNumericCellValue());
                            stringBuffer.append(",");
                        } else {
                            stringBuffer.append(cell.getStringCellValue());
                            stringBuffer.append(",");
                        }
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    bw.write(stringBuffer.toString());
                    bw.newLine();
                    System.out.println(stringBuffer);
                }
            }
            bw.flush();
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
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
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(waterTemperaturePlan.getId(), 1);
        if (CollectionUtils.isNotEmpty(waterTemperaturePlanFiles)) {
            List<String> fileKeys = waterTemperaturePlanFiles.stream()
                    .map(WaterTemperaturePlanFile::getFileKey)
                    .collect(Collectors.toList());
            List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
            if (CollectionUtils.isNotEmpty(fileItemPOS)) {
                for (FileItemPO fileItemPO : fileItemPOS) {
                    switch (fileItemPO.getFileType()) {
                        case WA_SCHEMA_PARAM_WEATHER:
                            planParamsResult.setWeatherFileKey(fileItemPO.getFileKey());
                            planParamsResult.setWeatherFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_INBOUND_TRAFFIC:
                            planParamsResult.setInboundTrafficFileKey(fileItemPO.getFileKey());
                            planParamsResult.setInboundTrafficFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_OUTBOUND_TRAFFIC:
                            planParamsResult.setOutboundTrafficFileKey(fileItemPO.getFileKey());
                            planParamsResult.setOutboundTrafficFileName(fileItemPO.getFilename());
                            break;
                        case WA_SCHEMA_PARAM_INBOUND_TEMP:
                            planParamsResult.setInboundTempFileKey(fileItemPO.getFileKey());
                            planParamsResult.setInboundTempFileName(fileItemPO.getFilename());
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
        planParamsResult.setBeta(waterTemperaturePlan.getBeta());
        planParamsResult.setSpread(waterTemperaturePlan.getSpread());
        planParamsResult.setExh(waterTemperaturePlan.getExh());
        planParamsResult.setInitialField(waterTemperaturePlan.getInitialField());
        return planParamsResult;
    }

    public void obtainDownLoadFile(WaterTemperaturePlan waterTemperaturePlan, Integer fileType, OutputStream outputStream) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileList(waterTemperaturePlan.getId(), fileType);
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        if (fileType == 2) {
            //输出结果文件
            List<FileItemPO> outExcelFiles = fileItemPOS.stream().filter(
                            fileItemPO -> fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL)
                                    || fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL))
                    .collect(Collectors.toList());
            String rootDir = System.getProperty("user.dir");
            String fileKey = UUID.randomUUID().toString();
            String file_dir_out = rootDir + "/script/ricen/dyout/"+fileKey+".xlsx";
            ExcelWriter excelWriter = EasyExcel.write(file_dir_out).build();
            for (int i = 0; i < outExcelFiles.size(); i++) {
                FileItemPO fileItemPO = outExcelFiles.get(i);
                Class head;
                if (fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL)){
                    head = VerticalWaterTemperature.class;
                } else {
                    head = DischargeWaterTemperature.class;
                }
                List<Object> objects = EasyExcel.read(fileItemPO.getPath()).head(head).sheet().doReadSync();
                excelWriter.write(objects, EasyExcel.writerSheet(i, fileItemPO.getFileType().getName()).build());
            }
            excelWriter.finish();
            ZipEntry zipEntry1 = new ZipEntry("计算结果.xlsx");
            Path path1 = Paths.get(file_dir_out);
            zipOutputStream.putNextEntry(zipEntry1);
            zipOutputStream.write(Files.readAllBytes(path1));
            zipOutputStream.closeEntry();
            for (FileItemPO fileItemPO : fileItemPOS) {
                String filePath = fileItemPO.getPath();
                ZipEntry zipEntry;
                if (fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_DIMENSIONAL_YT)) {
                    zipEntry = new ZipEntry(fileItemPO.getFileType().getName()+"_"+fileItemPO.getFilename());
                } else {
                    zipEntry = new ZipEntry(fileItemPO.getFileType().getName()+"."+fileItemPO.getExtension());
                }
                Path path = Paths.get(filePath);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(Files.readAllBytes(path));
                zipOutputStream.closeEntry();
            }
            String fileKeyDiff = UUID.randomUUID().toString();
            String file_dir_out_diff = rootDir + "/script/ricen/dyout/"+fileKeyDiff+".xlsx";
            //计算方差，残差
            ExcelWriter build = EasyExcel.write(file_dir_out_diff).build();
            this.verticalWaterTemperatureTest(waterTemperaturePlan, build);
            this.dischargeWaterTemperatureTest(waterTemperaturePlan, build);
            build.finish();
            //水库模型计算结果误差校验
            ZipEntry zipEntry = new ZipEntry("水库模型计算结果误差校验.xlsx");
            Path path = Paths.get(file_dir_out_diff);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(Files.readAllBytes(path));
            zipOutputStream.closeEntry();
        } else {
            //输出对比数据文件
            for (FileItemPO fileItemPO : fileItemPOS) {
                if (fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_COMP)) {
                    String filePath = fileItemPO.getPath();
                    ZipEntry zipEntry = new ZipEntry(fileItemPO.getFilename());
                    Path path = Paths.get(filePath);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(Files.readAllBytes(path));
                    zipOutputStream.closeEntry();
                }
            }
        }
        zipOutputStream.finish();
    }

    private void verticalWaterTemperatureTest(WaterTemperaturePlan waterTemperaturePlan, ExcelWriter excelWriter){
        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileListByPlanId(waterTemperaturePlan.getId());
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        List<FileItemPO> outExcelFiles = fileItemPOS.stream().filter(
                        fileItemPO -> fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL)
                                ||  fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_COMP))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(outExcelFiles)){
            return;
        }
        FileItemPO fileItemPOCXWD = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL))
                .findFirst().orElse(null);
        FileItemPO fileItemPOCXWDCom = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RIVER_COMP))
                .findFirst().orElse(null);
        if (fileItemPOCXWDCom == null){
            return;
        }
        List<VerticalWaterTemperature> verticalWaterTemperatureList = EasyExcel.read(fileItemPOCXWD.getPath())
                .sheet(0)
                .head(VerticalWaterTemperature.class)
                .doReadSync();
        assert fileItemPOCXWDCom != null;
        List<VerticalWaterTemperature> verticalWaterComTemperatureList = EasyExcel.read(fileItemPOCXWDCom.getPath())
                .sheet(0)
                .head(VerticalWaterTemperature.class)
                .doReadSync();
        List<VerticalWaterResidualTemp> verticalWaterResidualTemps = new ArrayList<>();
        for (VerticalWaterTemperature verticalWaterTemperature : verticalWaterTemperatureList) {
            VerticalWaterTemperature verticalWaterTemperatureCom = verticalWaterComTemperatureList.stream()
                    .filter(item -> item.equals(verticalWaterTemperature))
                    .findFirst().orElse(null);
            if (verticalWaterTemperatureCom != null) {
                VerticalWaterResidualTemp verticalWaterResidualTemp = new VerticalWaterResidualTemp();
                BeanUtils.copyProperties(verticalWaterTemperature, verticalWaterResidualTemp);
                verticalWaterResidualTemp.setComparativeTemperature(verticalWaterTemperatureCom.getTemperature());
                verticalWaterResidualTemp.setResidual(verticalWaterTemperature.getTemperature()- verticalWaterTemperatureCom.getTemperature());
                verticalWaterResidualTemps.add(verticalWaterResidualTemp);
            }
        }
        WriteSheet sheetCX = EasyExcel.writerSheet(0, "垂向水温-残差").head(VerticalWaterResidualTemp.class).build();
        excelWriter.write(verticalWaterResidualTemps, sheetCX);
        //EasyExcel.write(filePath).sheet(0, "垂向水温-残差").head(VerticalWaterResidualTemp.class).doWrite(verticalWaterResidualTemps);

        //方差逻辑
        List<Double> residualSqCollect = verticalWaterResidualTemps.stream()
                .map(item -> Math.pow(item.getResidual(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("方差", sum)
        );
        WriteSheet sheetCXfc = EasyExcel.writerSheet(1, "垂向水温-方差").build();
        excelWriter.write(data, sheetCXfc);
        System.out.println("------------------"+sum);
    }

    //下泄
    private void dischargeWaterTemperatureTest(WaterTemperaturePlan waterTemperaturePlan, ExcelWriter excelWriter){

        List<WaterTemperaturePlanFile> waterTemperaturePlanFiles = waterTemperaturePlanFileMapper.selectPlanFileListByPlanId(waterTemperaturePlan.getId());
        List<String> fileKeys = waterTemperaturePlanFiles.stream().map(WaterTemperaturePlanFile::getFileKey).collect(Collectors.toList());
        List<FileItemPO> fileItemPOS = fileItemMapper.queryFilesInKeys(fileKeys);
        List<FileItemPO> outExcelFiles = fileItemPOS.stream().filter(
                        fileItemPO -> fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL)
                                ||  fileItemPO.getFileType().equals(WA_SCHEMA_PARAM_RIVER_COMP))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(outExcelFiles)){
            return;
        }
        FileItemPO fileItemPOCXX = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL))
                .findFirst().orElse(null);
        FileItemPO fileItemPOCXWDCom = outExcelFiles.stream()
                .filter(item -> item.getFileType().equals(WA_SCHEMA_PARAM_RIVER_COMP))
                .findFirst().orElse(null);
        assert fileItemPOCXX != null;
        List<DischargeWaterTemperature> dischargeWaterTemperatureList = EasyExcel.read(fileItemPOCXX.getPath())
                .sheet(0)
                .head(DischargeWaterTemperature.class)
                .doReadSync();
        if (fileItemPOCXWDCom == null){
            return;
        }
        List<DischargeWaterTemperature> dischargeWaterComTemperatureList = EasyExcel.read(fileItemPOCXWDCom.getPath())
                .sheet(1)
                .head(DischargeWaterTemperature.class)
                .doReadSync();


        List<DischargeWaterResidualTemperature> dischargeWaterResidualTemperatures = new ArrayList<>();
        for (DischargeWaterTemperature dischargeWaterTemperature : dischargeWaterTemperatureList) {
            DischargeWaterTemperature dischargeWaterTemperatureCom = dischargeWaterComTemperatureList.stream()
                    .filter(item -> Objects.equals(item.getMonth(), dischargeWaterTemperature.getMonth()))
                    .findFirst().orElse(null);
            if (dischargeWaterTemperatureCom != null) {
                DischargeWaterResidualTemperature dischargeWaterResidualTemperature = new DischargeWaterResidualTemperature();
                BeanUtils.copyProperties(dischargeWaterTemperature, dischargeWaterResidualTemperature);
                dischargeWaterResidualTemperature.setComparativeTemperature(dischargeWaterTemperatureCom.getAverageTemperature());
                if (dischargeWaterTemperature.getAverageTemperature() == null){
                    dischargeWaterTemperature.setAverageTemperature(0.0);
                }
                if (dischargeWaterTemperatureCom.getAverageTemperature() == null){
                    dischargeWaterTemperatureCom.setAverageTemperature(0.0);
                }
                dischargeWaterResidualTemperature.setResidual(dischargeWaterTemperature.getAverageTemperature()-dischargeWaterTemperatureCom.getAverageTemperature());
                dischargeWaterResidualTemperatures.add(dischargeWaterResidualTemperature);
            }
        }
        WriteSheet sheetXX = EasyExcel.writerSheet(2, "下泄水温-残差").head(DischargeWaterResidualTemperature.class).build();
        excelWriter.write(dischargeWaterResidualTemperatures, sheetXX);
        //方差逻辑
        List<Double> residualSqCollect = dischargeWaterResidualTemperatures.stream()
                .map(item -> Math.pow(item.getResidual(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("方差", sum)
        );
        WriteSheet sheetXXFC = EasyExcel.writerSheet(3, "下泄水温-方差").build();
        excelWriter.write(data, sheetXXFC);
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);
    }
}
