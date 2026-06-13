package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DischargeWaterResidualTemperature {

    @ExcelProperty(value = "Month")
    private Integer month;
    @ExcelProperty(value = "AverageTemperature")
    private Double averageTemperature;

    @ExcelProperty(value = "Comparative Temperature")
    private Double comparativeTemperature;

    @ExcelProperty("residual")
    private Double residual;
}
