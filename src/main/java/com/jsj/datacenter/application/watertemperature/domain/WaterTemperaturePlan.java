package com.jsj.datacenter.application.watertemperature.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("water_temperature_plan")
@Data
public class WaterTemperaturePlan {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String planName;
    private Integer planType;
    private Integer progressStatus;
    private String process;
    private Date planStartTime;
    private Date planEndTime;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
    private Double exh;
    private Double beta;
    private Double spread;                      //扩散
    private Double initialLevel;
    private Integer totalDay;
    private Double initialField;

    //河道模型预设参数
    private Double solarcoe;
    private Double mecovertcoe;
    private Double windcoe;

}
