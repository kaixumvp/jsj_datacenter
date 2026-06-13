package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

@Data
public class VerticalWaterTemperature {

    @ExcelProperty(value = "Date")
    @DateTimeFormat("yyyy-MM")
    private LocalDate date;

    @ExcelProperty("Depth")
    private Double depth;

    @ExcelProperty("Temperature")
    private Double temperature;

    @ExcelProperty("Water_Level")
    private Double waterLevel;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VerticalWaterTemperature that = (VerticalWaterTemperature) o;
        return Objects.equals(date, that.date) && Objects.equals(depth, that.depth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, depth);
    }
}
