package com.jsj.datacenter.adapter.dto.environment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 砂石设备监测数据响应（包含最新数据和历史数据）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("砂石设备监测数据响应")
public class SandEquipmentMonitorResp {
    
    @ApiModelProperty("5类设备的最新数据列表")
    private List<EnvironmentMonitorNewVo> latestDataList;
    
    @ApiModelProperty("历史数据列表（根据筛选条件，用于图表展示）")
    private List<EnvironmentMonitorNewVo> historyDataList;
}
