package com.jsj.datacenter.application.watertemperature.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("water_temperature_plan_file_items")
@Data
public class WaterTemperaturePlanFile {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField
    private Integer planId;
    @TableField
    private Integer fileId;
    @TableField
    private String fileKey;
    @TableField
    private Integer fileType;
}
