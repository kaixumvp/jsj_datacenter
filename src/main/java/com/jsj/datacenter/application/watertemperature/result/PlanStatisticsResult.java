package com.jsj.datacenter.application.watertemperature.result;

import lombok.Data;

@Data
public class PlanStatisticsResult {

    private int totals;
    private int successNum;
    private int failNum;
    private int inProcessNum;
}
