package com.jsj.datacenter.application.temprature.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("temperature_error_log")
@Data
public class TemperatureErrorLog {
    private String sn;

    private String date;

    private Double maxTemp;

    private Double minTemp;

    private Double tempDiff;
}
