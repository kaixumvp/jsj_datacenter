package com.jsj.datacenter.application.environment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 砂石设备监测数据实体
 */
@Data
@TableName("sand_equipment_monitor")
public class SandEquipmentMonitor implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 设备地址/设备ID
     */
    private Integer deviceId;
    
    /**
     * 节点
     */
    private Integer node;
    
    /**
     * PM2.5 (ug/m³)
     */
    private Float pm2;
    
    /**
     * PM10 (ug/m³)
     */
    private Float pm10;
    
    /**
     * 总悬浮颗粒物 (ug/m³)
     */
    private Float tsp;
    
    /**
     * 噪声 (dB)
     */
    private Float noise;
    
    /**
     * 温度
     */
    private Float temperature;
    
    /**
     * 湿度
     */
    private Float humidity;
    
    /**
     * 风力 (m/s)
     */
    private Float windPower;
    
    /**
     * 风速 (m/s)
     */
    private Float windSpeed;
    
    /**
     * 风向(方位数值)
     */
    private Float windDirect;
    
    /**
     * 风向(角度)
     */
    private Float windDirectDegrees;
    
    /**
     * 经度
     */
    private Float lng;
    
    /**
     * 纬度
     */
    private Float lat;
    
    /**
     * 坐标类型：0-百度经纬度,1-联通基站,2-移动基站,3-GPS
     */
    private Integer coordinateType;
    
    /**
     * 继电器状态
     */
    private String relayStatus;
    
    /**
     * 数据采集时间
     */
    private LocalDateTime createTime;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备位置
     */
    private String position;
    
    /**
     * 风向(中文描述)
     */
    private String windDir;
    
    /**
     * 数据同步时间
     */
    private LocalDateTime syncTime;
}
