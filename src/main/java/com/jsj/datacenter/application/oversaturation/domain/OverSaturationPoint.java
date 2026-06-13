package com.jsj.datacenter.application.oversaturation.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("over_saturation_point")
@Data
public class OverSaturationPoint {

    @TableId(type = IdType.AUTO)
    private Integer id;


    private String period;

    private String pointName;

    private String data;

    private String createTime;

    private String createBy;
}
