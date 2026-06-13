package com.jsj.datacenter.application.watertemperature;

import lombok.Data;

@Data
public class WaterTemperatureReportsParams {
    public String reportName;
    private Integer pageNum;
    public Integer pageSize;
}
