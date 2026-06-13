package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RiverChannelMainWaterTemp {
    @ExcelProperty("时间序号")
    private double dataIndex;
    @ExcelProperty("水温")
    private double waterTemp;

}
