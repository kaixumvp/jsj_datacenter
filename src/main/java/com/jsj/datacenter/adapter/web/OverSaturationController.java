package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONArray;
import com.google.common.collect.Lists;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.oversaturation.OverSaturationDTO;
import com.jsj.datacenter.application.oversaturation.service.OverSaturationService;
import com.jsj.datacenter.infrastructure.vo.OverSaturationItemVO;
import com.jsj.datacenter.infrastructure.vo.PointPositionItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(tags = "过饱和监测接口")
@RestController()
@RequestMapping("over-saturation")
@EasyResponse
public class OverSaturationController {

    @Autowired
    OverSaturationService overSaturationService;

    @GetMapping("parse/{fileKey}")
    @ApiOperation("解析过饱和监测数据")
    public OverSaturationDTO parse(@PathVariable String fileKey) {
        return overSaturationService.parseOverSaturation(fileKey);
    }


    @PostMapping("add")
    @ApiOperation("新增过饱和监测数据")
    public void addOverSaturation(@RequestBody OverSaturationDTO request) {
        overSaturationService.addOverSaturation(request);
    }

    @PutMapping("edit")
    @ApiOperation("编辑过饱和监测数据")
    public void editOverSaturation(@RequestBody OverSaturationDTO request) {
        overSaturationService.editOverSaturation(request);
    }

    @GetMapping("query")
    @ApiOperation("查询过饱和监测数据")
    public ResponseEntity<List<OverSaturationDTO>> queryOverSaturation(@RequestParam(required = false) String period) {
        List<OverSaturationDTO> overSaturationList;
        if (period == null || period.isEmpty()) {
            overSaturationList = overSaturationService.getAllOverSaturation();
        }
        else {
            OverSaturationDTO overSaturationByPeriod = overSaturationService.getOverSaturationByPeriod(period);
            overSaturationList = new ArrayList<>();
            overSaturationList.add(overSaturationByPeriod);
        }
        return ResponseEntity.ok(overSaturationList);
    }

    @DeleteMapping("delete")
    @ApiOperation("删除过饱和监测数据")
    public void deleteOverSaturation(@RequestParam String period) {
        overSaturationService.deleteOverSaturation(period);
    }

    @GetMapping("periods")
    @ApiOperation("获取所有过饱和监测周期")
    public ResponseEntity<List<String>> getAllPeriods() {
        List<String> periods = overSaturationService.getAllPeriods();
        return ResponseEntity.ok(periods);
    }
}
