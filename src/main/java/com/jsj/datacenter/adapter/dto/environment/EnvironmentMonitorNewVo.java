package com.jsj.datacenter.adapter.dto.environment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境监测数据VO
 */
@Data
@ApiModel("环境监测数据")
public class EnvironmentMonitorNewVo {
    
    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("设备地址/设备ID")
    private Integer deviceId;
    
    @ApiModelProperty("节点")
    private Integer node;
    
    @ApiModelProperty("PM2.5 (ug/m³)")
    private Float pm2;
    
    @ApiModelProperty("PM10 (ug/m³)")
    private Float pm10;
    
    @ApiModelProperty("总悬浮颗粒物 (ug/m³)")
    private Float tsp;
    
    @ApiModelProperty("噪声 (dB)")
    private Float noise;
    
    @ApiModelProperty("温度")
    private Float temperature;
    
    @ApiModelProperty("湿度")
    private Float humidity;
    
    @ApiModelProperty("风力 (m/s)")
    private Float windPower;
    
    @ApiModelProperty("风速 (m/s)")
    private Float windSpeed;
    
    @ApiModelProperty("风向(方位数值)")
    private Float windDirect;
    
    @ApiModelProperty("风向(角度)")
    private Float windDirectDegrees;
    
    @ApiModelProperty("经度")
    private Float lng;
    
    @ApiModelProperty("纬度")
    private Float lat;
    
    @ApiModelProperty("坐标类型：0-百度经纬度,1-联通基站,2-移动基站,3-GPS")
    private Integer coordinateType;
    
    @ApiModelProperty("继电器状态")
    private String relayStatus;
    
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ApiModelProperty("设备名称")
    private String deviceName;
    
    @ApiModelProperty("设备位置")
    private String position;
    
    @ApiModelProperty("风向(中文描述)")
    private String windDir;
}
