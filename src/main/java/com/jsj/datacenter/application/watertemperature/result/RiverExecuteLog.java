package com.jsj.datacenter.application.watertemperature.result;

import lombok.Data;

import java.util.Date;

@Data
public class RiverExecuteLog {
    private Integer planId;
    private String planName;
    private Integer planType;
    private Integer progressStatus;
    private Integer totalDays;  //总天数
    private String process;
    private Long createTime;
}
