package com.jsj.datacenter.adapter.dto.river;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.util.Date;

@Data
public class VerticalWaterTempCondition {
    //@ExcelProperty("Date")
    private String dateStr;

    private Integer month;
    @ExcelProperty("表层水温")
    private Double surfaceWaterTemp;
    @ExcelProperty("底层水温")
    private Double bottomWaterTemp;
    @ExcelProperty("表底温差")
    private Double verticalTempDifference;
    @ExcelProperty("平均水温")
    private Double agvWaterTemp;
    @ExcelProperty("表层温跃层厚度")
    private Double thickness;
}
