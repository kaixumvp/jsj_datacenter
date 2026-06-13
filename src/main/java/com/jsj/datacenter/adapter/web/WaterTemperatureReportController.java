package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.river.WaterTemperatureReportResult;
import com.jsj.datacenter.adapter.dto.river.WaterTemperatureReportVO;
import com.jsj.datacenter.application.watertemperature.WaterTemperatureReportsParams;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperatureReports;
import com.jsj.datacenter.application.watertemperature.service.WaterTemperatureReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "水温报告接口")
@RestController
@RequestMapping("waterTemperatureReport")
@EasyResponse
@Slf4j
public class WaterTemperatureReportController {

    @Autowired
    WaterTemperatureReportService waterTemperatureReportService;

    @PostMapping("query")
    @ApiOperation(value = "水温报告列表")
    public Page<WaterTemperatureReports> queryWaterTemperatureReport(@RequestBody WaterTemperatureReportsParams waterTemperatureReportsParams) {
        return waterTemperatureReportService.findWaterTemperatureReportsPage(waterTemperatureReportsParams);
    }

    @PostMapping("add")
    @ApiOperation(value = "添加水温报告")
    public JSONObject addWaterTemperatureReport(@RequestBody WaterTemperatureReportVO waterTemperatureReportVO) {
        WaterTemperatureReports waterTemperatureReports = waterTemperatureReportService.submitWaterTemperatureReports(waterTemperatureReportVO);
        return JSONObject.of("reportId", waterTemperatureReports.getId());
    }

    @GetMapping("info/{reportId}")
    @ApiOperation(value = "水温报告生成")
    public WaterTemperatureReports info(@PathVariable(name = "reportId") Long reportId){
        return waterTemperatureReportService.getInfo(reportId);
    }

    @DeleteMapping("delete/{reportId}")
    @ApiOperation(value = "水温报告生成")
    public void delete(@PathVariable(name = "reportId") Long reportId){
        waterTemperatureReportService.deleteReport(reportId);
    }

    @GetMapping("statistics")
    @ApiOperation(value = "统计水温报告")
    public WaterTemperatureReportResult reportStatistics() {
        return waterTemperatureReportService.getReportCountClassification();
    }

    @GetMapping("report/{reportId}")
    @ApiOperation(value = "水温报告生成")
    public JSONObject reporting(@PathVariable(name = "reportId") Long reportId) {
        String path = waterTemperatureReportService.reporting(reportId);
        return JSONObject.of("path", path);
    }



}
