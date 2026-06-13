package com.jsj.datacenter.application.watertemperature.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("water_temperature_reports")
@Data
public class WaterTemperatureReports {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField
    private String reportName;
    @TableField
    private Date reportCreateTime;
    @TableField
    private String relatedTaskIds;
    @TableField
    private Integer reportStatus;
    @TableField
    private Date createTime;
    @TableField
    private Date updateTime;
    @TableField
    private String creator;
    @TableField
    private String reportUrl;

}
