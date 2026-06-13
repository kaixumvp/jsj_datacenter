package com.jsj.datacenter.adapter.dto.environment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 砂石设备监测数据查询请求
 */
@Data
@ApiModel("砂石设备监测数据查询请求")
public class SandEquipmentMonitorQueryReq {
    
    @ApiModelProperty("设备ID（可选，不传则查询所有设备）")
    private Integer deviceId;
    
    @ApiModelProperty("开始日期（格式：yyyy-MM-dd）")
    private String startDate;
    
    @ApiModelProperty("结束日期（格式：yyyy-MM-dd）")
    private String endDate;
}
