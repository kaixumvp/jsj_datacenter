package com.jsj.datacenter.application.oversaturation.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@TableName("over_saturation")
@Data
public class OverSaturation {

    private String period;

    private String fileKey;

    private String data;

    @TableField(exist = false)
    private List<OverSaturationPoint> overSaturationPoints;

    private String createTime;

    private String createBy;
}
