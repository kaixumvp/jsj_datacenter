package com.jsj.datacenter.application.waterqulity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("water_quality")
@Data
public class WaterQuality {

    private String period;

    private String fileKey;

    private String data;

    private String createTime;

    private String createBy;
}
