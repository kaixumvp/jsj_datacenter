package com.jsj.datacenter.adapter.dto.river;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DischargeWaterConditionDay {
    @ExcelProperty("DayInYear")
    private Integer day ;
    @ExcelProperty("Temperature")
    private Double temperature ;
}
