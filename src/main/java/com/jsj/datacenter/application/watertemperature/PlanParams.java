package com.jsj.datacenter.application.watertemperature;

import lombok.Data;

@Data
public class PlanParams {
    public String planName;
    private Integer pageNum;
    public Integer pageSize;
}
