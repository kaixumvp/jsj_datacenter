package com.jsj.datacenter.application.watertemperature.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.application.watertemperature.PlanParams;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanFileMapper;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanMapper;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.application.watertemperature.result.PlanStatisticsResult;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class WaterTemperaturePlanService {

    @Autowired
    private RiverService riverService;

    @Autowired
    private WaterTemperaturePlanMapper waterTemperaturePlanMapper;

    @Autowired
    private WaterTemperaturePlanFileMapper waterTemperaturePlanFileMapper;

    @Autowired
    private ReservoirService reservoirService;

    /**
     * 执行方案任务
     * @param riverCalculate
     */
    public WaterTemperaturePlan executeTaskCalculate(RiverCalculateVO riverCalculate) {

        Integer planType = riverCalculate.getPlanType();
        WaterTemperaturePlan waterTemperaturePlan =  new WaterTemperaturePlan();
        if (planType == 3 || planType == 5) {
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
            waterTemperaturePlan.setTotalDay(riverCalculate.getTotalDay());
            waterTemperaturePlan.setProgressStatus(1);
            waterTemperaturePlan.setProcess("任务开始");
            waterTemperaturePlan.setInitialField(riverCalculate.getInitialField());
            waterTemperaturePlanMapper.insert(waterTemperaturePlan);
            riverService.executeScript(riverCalculate, waterTemperaturePlan);
            return waterTemperaturePlan;
        }else {
            return reservoirService.calculate(riverCalculate);
        }
    }

    public Page<WaterTemperaturePlan> queryPlanList(PlanParams planParams) {
        int startNum = (planParams.getPageNum() - 1) * planParams.getPageSize();
        int i = waterTemperaturePlanMapper.selectListLikeNameCount(planParams.getPlanName());
        List<WaterTemperaturePlan> waterTemperaturePlans = waterTemperaturePlanMapper.selectListLikeNameLimitPage(planParams.getPlanName(), startNum, planParams.getPageSize());
        for (WaterTemperaturePlan waterTemperaturePlan : waterTemperaturePlans) {
            if (waterTemperaturePlan.getProgressStatus() == 6) {
                waterTemperaturePlan.setProgressStatus(-2);
            }
        }
        Page<WaterTemperaturePlan> waterTemperaturePlanPage = new Page<>();
        waterTemperaturePlanPage.setSize(planParams.getPageSize());
        waterTemperaturePlanPage.setRecords(waterTemperaturePlans);
        waterTemperaturePlanPage.setCurrent(planParams.getPageNum());
        waterTemperaturePlanPage.setTotal(i);
        return waterTemperaturePlanPage;
    }
    public  List<WaterTemperaturePlan> queryPlanListByType(Integer planType){
        LambdaQueryWrapper<WaterTemperaturePlan> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(WaterTemperaturePlan::getPlanType, planType);
        return waterTemperaturePlanMapper.selectList(lambdaQuery);
    }

    public RiverCalculateResult obtainCalculate(Integer planId) {
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        if (waterTemperaturePlan.getPlanType() == 1 || waterTemperaturePlan.getPlanType() == 3 || waterTemperaturePlan.getPlanType() == 5) {
            return riverService.obtainCalculate(planId);
        } else if (waterTemperaturePlan.getPlanType() == 4 || waterTemperaturePlan.getPlanType() == 2) {
            return reservoirService.obtainCalculate(planId);
        } else {
            throw new ServiceException("计划类型有误");
        }
    }

    public void deletePlan(Integer planId) {
        waterTemperaturePlanMapper.deleteById(planId);
        Map<String, Object> columnMap = Maps.newHashMap();
        columnMap.put("plan_id", planId);
        waterTemperaturePlanFileMapper.deleteByMap(columnMap);
    }


    public PlanStatisticsResult planStatistics() {
        PlanStatisticsResult planStatisticsResult = new PlanStatisticsResult();
        planStatisticsResult.setTotals(waterTemperaturePlanMapper.totals());
        planStatisticsResult.setSuccessNum(waterTemperaturePlanMapper.successNum());
        planStatisticsResult.setFailNum(waterTemperaturePlanMapper.failNum());
        planStatisticsResult.setInProcessNum(waterTemperaturePlanMapper.inProcessNum());
        return planStatisticsResult;
    }

    public PlanParamsResult getPlanParams(Integer planId, Integer planType) {
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        if (waterTemperaturePlan == null) {
            return new PlanParamsResult();
        }
        if (waterTemperaturePlan.getPlanType() == 3 || waterTemperaturePlan.getPlanType() ==1 || waterTemperaturePlan.getPlanType() ==5) {
            return riverService.getPlanParams(planId, planType);
        }else  if (waterTemperaturePlan.getPlanType() == 4 || waterTemperaturePlan.getPlanType() == 2) {
            return reservoirService.getPlanParams(planId, planType);
        }
        return new PlanParamsResult();
    }

    public void obtainDataZipByPlanIdAndFileType(Integer planId, Integer fileType, OutputStream outputStream) throws IOException {
        //获取方案详情
        WaterTemperaturePlan waterTemperaturePlan = waterTemperaturePlanMapper.selectById(planId);
        switch (waterTemperaturePlan.getPlanType()) {
            case 1:
            case 3:
            case 5:
                riverService.obtainDownLoadFile(waterTemperaturePlan, fileType, outputStream);
                break;
            case 2:
            case 4:
                reservoirService.obtainDownLoadFile(waterTemperaturePlan, fileType, outputStream);
                break;

        }
        //根据不同的方案获取不同的数据结果
                //河道方案类型，需要计算时解析结果文件存储excle,再进行
        //文件打包zip
    }

    public static void copyScriptDir(Integer planId, Integer planType) throws IOException {
        //3,水库 方案； 4河道方案； 5水库有档方案
        String rootDir = System.getProperty("user.dir");
        String baseDir = rootDir + "script";
        if (planType == 4) {
            String sourceDir = baseDir + "ricen";
            String destinationDir = baseDir + "ricen" + "_" + planId;
            copyFolder(Paths.get(sourceDir), Paths.get(destinationDir));
        }
    }

    public static void copyFolder(Path sourceDir, Path destinationDir) throws IOException {
        Files.walk(sourceDir)
                .forEach(sourcePath -> {
                    Path destinationPath = destinationDir.resolve(sourceDir.relativize(sourcePath));
                    try {
                        if (Files.isDirectory(sourcePath)) {
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS);
                        } else {
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        System.err.println("复制文件失败：" + sourcePath + " -> " + destinationPath + ": " + e.getMessage());
                    }
                });
    }


}
