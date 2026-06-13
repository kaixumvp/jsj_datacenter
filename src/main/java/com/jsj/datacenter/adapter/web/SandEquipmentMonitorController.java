package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.dto.environment.EnvironmentMonitorEchartsResp;
import com.jsj.datacenter.adapter.dto.environment.EnvironmentMonitorNewVo;
import com.jsj.datacenter.adapter.dto.environment.SandEquipmentMonitorQueryReq;
import com.jsj.datacenter.adapter.dto.environment.SandEquipmentMonitorResp;
import com.jsj.datacenter.application.environment.service.SandEquipmentMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 砂石设备监测数据管理接口
 */
@Slf4j
@Api(tags = "砂石设备监测数据管理")
@RestController
@RequestMapping("/api/sand-monitor")
public class SandEquipmentMonitorController {
    
    @Autowired
    private SandEquipmentMonitorService monitorService;
    
    /**
     * 手动触发数据同步
     */
    @PostMapping("/sync")
    @ApiOperation("手动触发砂石设备监测数据同步")
    public String manualSync() {
        log.info("收到手动同步请求");
        
        try {
            int savedCount = monitorService.syncMonitorData();
            return String.format("同步成功，共同步 %d 条数据", savedCount);
        } catch (Exception e) {
            log.error("手动同步失败: {}", e.getMessage(), e);
            return "同步失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取监测数据（包含最新数据和历史数据）
     */
    @GetMapping("/data")
    @ApiOperation("获取砂石设备监测数据")
    public SandEquipmentMonitorResp getMonitorData(SandEquipmentMonitorQueryReq queryReq) {
        log.info("收到查询请求: deviceId={}, startDate={}, endDate={}", 
                queryReq.getDeviceId(), queryReq.getStartDate(), queryReq.getEndDate());
        return monitorService.getMonitorData(queryReq);
    }
    
    /**
     * 根据设备ID获取所有的历史数据
     */
    @GetMapping("/history")
    @ApiOperation("根据设备ID获取砂石设备历史监测数据")
    public List<EnvironmentMonitorNewVo> getHistoryData(SandEquipmentMonitorQueryReq queryReq) {
        log.info("收到历史数据查询请求: deviceId={}, startDate={}, endDate={}", 
                queryReq.getDeviceId(), queryReq.getStartDate(), queryReq.getEndDate());
        return monitorService.getHistoryData(queryReq);
    }
    
    /**
     * 获取历史数据的ECharts格式（用于图表展示）
     */
    @GetMapping("/history/echarts")
    @ApiOperation("获取砂石设备历史监测数据ECharts格式")
    public EnvironmentMonitorEchartsResp getHistoryDataForEcharts(SandEquipmentMonitorQueryReq queryReq) {
        log.info("收到ECharts格式历史数据查询请求: deviceId={}, startDate={}, endDate={}", 
                queryReq.getDeviceId(), queryReq.getStartDate(), queryReq.getEndDate());
        return monitorService.getHistoryDataForEcharts(queryReq);
    }

    /**
     * 获取所有设备的最新数据
     */
    @GetMapping("/latest")
    @ApiOperation("获取所有砂石设备最新监测数据")
    public List<EnvironmentMonitorNewVo> getLatestData() {
        log.info("收到最新数据查询请求");
        return monitorService.getLatestData();
    }
}
