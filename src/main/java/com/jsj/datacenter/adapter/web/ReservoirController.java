package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.application.watertemperature.service.ReservoirService;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@Api(tags = "河道模型接口")
@RestController
@RequestMapping("reservoir")
@EasyResponse
@Slf4j
public class ReservoirController {

    @Autowired
    private ReservoirService reservoirService;

    @PostMapping("calculate")
    @ApiOperation(value = "河道模型计算")
    public JSONObject reservoirCalculate(@RequestBody RiverCalculateVO riverCalculate) {
        log.info("河道模型计算参数-{}", JSONObject.toJSONString(riverCalculate));
        riverCalculate.setPlanName("河道模型");
        riverCalculate.setPlanType(2);
        WaterTemperaturePlan calculate;
        try {
            calculate = reservoirService.calculate(riverCalculate);
        }catch (Exception e){
            throw new ServiceException("河道模型执行任务创建失败");
        }
        return JSONObject.of("planId", calculate.getId());
    }

    @PostMapping("obtainCalculate")
    @ApiOperation(value = "获取计算结果")
    public RiverCalculateResult obtainCalculate(@RequestParam(value = "planId") Integer planId) {
        return reservoirService.obtainCalculate(planId);
    }

    @PostMapping("obtainTaskParam")
    @ApiOperation(value = "获取参数详情接口")
    public PlanParamsResult obtainTaskParam(@RequestParam(value = "planId", required = false) Integer planId, @RequestParam(value = "planType", required = false) Integer planType){
        if(planId == null && planType == null){
            planType = 2;
        }
        return reservoirService.obtainTaskParam(planId, planType);
    }

    @GetMapping("download")
    @ApiOperation(value = "下载文件")
    public void queryFileList(HttpServletResponse response) {
        try {
            String rootDir = System.getProperty("user.dir");


            File targetFile = new File(rootDir + "/script/template/河道模型对比数据模板.xlsx");
            // 将文件名UrlEncode
            String fileName = URLEncoder.encode("river.xlsx", "UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");
            response.setContentLength((int) targetFile.length());

            Files.copy(targetFile.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new ServiceException("文件下载失败");
        }
    }
}
