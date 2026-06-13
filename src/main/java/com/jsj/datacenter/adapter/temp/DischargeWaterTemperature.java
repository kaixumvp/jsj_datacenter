package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DischargeWaterTemperature {

    @ExcelProperty(value = "Month")
    private Integer month;
    @ExcelProperty(value = "AverageTemperature")
    private Double averageTemperature;
}
