package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.waterquality.WaterQualityDTO;
import com.jsj.datacenter.application.waterqulity.service.WaterQualityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "水质监测接口")
@RestController()
@RequestMapping("water-quality")
@EasyResponse
public class WaterQualityController {

    @Autowired
    WaterQualityService waterQualityService;


    @PostMapping("add")
    @ApiOperation("新增水质监测数据")
    public void addWaterQuality(@RequestBody WaterQualityDTO request) {
        waterQualityService.addWaterQuality(request);
    }

    @PutMapping("edit")
    @ApiOperation("编辑水质监测数据")
    public void editWaterQuality(@RequestBody WaterQualityDTO request) {
        waterQualityService.editWaterQuality(request);
    }

    @GetMapping("query")
    @ApiOperation("查询水质监测数据")
    public ResponseEntity<List<WaterQualityDTO>> queryWaterQuality(@RequestParam(required = false) String period) {
        List<WaterQualityDTO> waterQualityList;
        if (period == null || period.isEmpty()) {
            waterQualityList = waterQualityService.getAllWaterQuality();
        }
        else {
            WaterQualityDTO waterQualityByPeriod = waterQualityService.getWaterQualityByPeriod(period);
            waterQualityList = new ArrayList<>();
            waterQualityList.add(waterQualityByPeriod);
        }
        return ResponseEntity.ok(waterQualityList);
    }

    @DeleteMapping("delete")
    @ApiOperation("删除水质监测数据")
    public void deleteWaterQuality(@RequestParam String period) {
        waterQualityService.deleteWaterQuality(period);
    }
    @GetMapping("periods")
    @ApiOperation("获取所有水质监测周期")
    public ResponseEntity<List<String>> getAllPeriods() {
        List<String> periods = waterQualityService.getAllPeriods();
        return ResponseEntity.ok(periods);
    }
}
