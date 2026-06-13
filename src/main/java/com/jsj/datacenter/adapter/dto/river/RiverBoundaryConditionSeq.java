package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

@Data
public class RiverBoundaryConditionSeq {
    // 月份
    private Integer seq;
    // 电站下泄流量
    private Double dischargedFlow;
    // 电站下泄水温
    private Double dischargedWaterTemperature;
    // 定曲河流量
    private Double branchFlow;
    // 定曲河水温
    private Double branchWaterTemperature;
    // 气温
    private Double airTemperature;
}
