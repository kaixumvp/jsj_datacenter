package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VerticalWaterResidualTemp {

    @ExcelProperty(value = "sequenceNumber")
    private Integer sequenceNumber;

    @ExcelProperty(value = "Date")
    @DateTimeFormat("yyyy-MM")
    private LocalDate date;

    @ExcelProperty("Depth")
    private Double depth;

    @ExcelProperty("Temperature")
    private Double temperature;

    @ExcelProperty("Comparative Temperature")
    private Double comparativeTemperature;

    @ExcelProperty("Water_Level")
    private Double waterLevel;

    @ExcelProperty("residual")
    private Double residual;
}
