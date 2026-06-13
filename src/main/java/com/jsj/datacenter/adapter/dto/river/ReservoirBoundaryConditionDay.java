package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

@Data
public class ReservoirBoundaryConditionDay {
    // 月份
    private Integer day;

    // 入库流量
    private Double inboundFlow;

    // 出库流量
    private Double outboundFlow;

    // 入库水温
    private Double inboundTemperature;

    //  气温
    private Double airTemperature;

    //水库水位
    private Double reservoirLevel;
}
