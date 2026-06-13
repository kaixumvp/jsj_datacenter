package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONArray;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.request.DeviceDateInfoReqDTO;
import com.jsj.datacenter.adapter.dto.request.DeviceInfoReqDTO;
import com.jsj.datacenter.adapter.dto.response.TemperatureErrorLogDTO;
import com.jsj.datacenter.application.DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Api(tags = "数据接口")
@RestController()
@RequestMapping("/data")
@EasyResponse
public class DataController {

    @Autowired
    private DataService dataService;

    @ApiOperation(value = "获取设备当前信息")
    @PostMapping("device-info")
    public JSONArray getDeviceInfo(@RequestBody DeviceInfoReqDTO reqDTO) {
        return dataService.getDeviceInfo(reqDTO.getSn());
    }

    @ApiOperation(value = "获取设备历史信息")
    @PostMapping("date-info")
    public JSONArray getData(@RequestBody @Validated DeviceDateInfoReqDTO reqDTO) {
        return dataService.getDateInfo(reqDTO);
    }

    @ApiOperation(value = "获取异常设备温度日志")
    @PostMapping("temperature-error-log")
    public List<TemperatureErrorLogDTO> getTemperatureErrorLogs(@RequestBody DeviceInfoReqDTO reqDTO) {
        return dataService.getTemperatureErrorLogs(reqDTO.getSn());
    }
}
