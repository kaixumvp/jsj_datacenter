package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RiverChannelFlowTem {
    @ExcelProperty("时间序号")
    private double dateIndex;
    @ExcelProperty("上游水位")
    private double upstreamWaterLevel;
    @ExcelProperty("上游流量")
    private double upstreamFlow;
    @ExcelProperty("下游水位")
    private double downstreamWaterLevel;
    @ExcelProperty("下游流量")
    private double downstreamFlow;
}
