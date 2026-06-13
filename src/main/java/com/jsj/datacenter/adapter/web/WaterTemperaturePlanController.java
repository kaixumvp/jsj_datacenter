package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.application.watertemperature.PlanParams;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.application.watertemperature.result.PlanStatisticsResult;
import com.jsj.datacenter.application.watertemperature.service.WaterTemperaturePlanService;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api(tags = "水库河道方案接口")
@RestController
@RequestMapping("waterTemperaturePlan")
@EasyResponse
@Slf4j
public class WaterTemperaturePlanController {

    @Autowired
    private WaterTemperaturePlanService waterTemperaturePlanService;

    @PostMapping("addPlan")
    @ApiOperation(value = "添加方案")
    public JSONObject addPlan(@RequestBody RiverCalculateVO riverCalculate){
        if (riverCalculate.getPlanType() == null || riverCalculate.getPlanType() == 1 || riverCalculate.getPlanType() == 2) {
            throw new ServiceException("planType 参数输入错误； 只能为3或者4");
        }
        WaterTemperaturePlan waterTemperaturePlan;
        try {
            waterTemperaturePlan = waterTemperaturePlanService.executeTaskCalculate(riverCalculate);
        } catch (Exception e) {
            throw new ServiceException("方案创建失败");
        }
        return JSONObject.of("planId", waterTemperaturePlan.getId());
    }

    @PostMapping("listPlans")
    @ApiOperation(value = "方案列表")
    public Page<WaterTemperaturePlan> listPlans(@RequestBody(required = false) PlanParams planParams) {
        if (planParams == null) {
            planParams = new PlanParams();
        }
        if (planParams.getPlanName() == null){
            planParams.setPlanName("");
        }
        if (planParams.getPageNum() == null || planParams.getPageNum() <= 0) {
            planParams.setPageNum(1);
        }
        if (planParams.getPageSize() == null || planParams.getPageSize() <= 0) {
            planParams.setPageSize(5);
        }
        return waterTemperaturePlanService.queryPlanList(planParams);
    }

    @PostMapping("obtainCalculate")
    @ApiOperation(value = "获取方案结果")
    public RiverCalculateResult obtainCalculate(@RequestParam("planId") Integer planId){
        return waterTemperaturePlanService.obtainCalculate(planId);
    }

    @PostMapping("deletePlan")
    @ApiOperation(value = "删除方案")
    public String deletePlan(@RequestParam("planId") Integer planId) {
        try {
            waterTemperaturePlanService.deletePlan(planId);
            return "删除成功";
        } catch (ServiceException e) {
            throw new ServiceException("删除异常");
        }
    }

    @PostMapping("planStatistics")
    @ApiOperation(value = "方案统计")
    public PlanStatisticsResult planStatistics() {
        return waterTemperaturePlanService.planStatistics();
    }

    @PostMapping("obtainTaskParam")
    @ApiOperation(value = "获取参数详情接口")
    public PlanParamsResult obtainTaskParam(@RequestParam(value = "planId", required = false) Integer planId, @RequestParam(value = "planType", required = false) Integer planType){
        return waterTemperaturePlanService.getPlanParams(planId, planType);
    }

    @GetMapping("obtainPlans/{type}")
    @ApiOperation(value = "方案列表")
    public List<WaterTemperaturePlan> obtainPlanListByType(@PathVariable Integer type) {
        return waterTemperaturePlanService.queryPlanListByType(type);
    }

    @GetMapping("/{planId}/download/{fileType}")
    @ApiOperation(value = "下载文件")
    public void downloadDataZip(HttpServletResponse response,
                                @PathVariable("planId")Integer planId,
                                @PathVariable("fileType")Integer fileType) throws IOException {

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=downloadData.zip");
        waterTemperaturePlanService.obtainDataZipByPlanIdAndFileType(planId, fileType, response.getOutputStream());
    }

}
