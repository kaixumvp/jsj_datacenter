package com.jsj.datacenter.adapter.dto.river;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DischargeNaturalWaterConditionDay {

    @ExcelProperty(index = 0)
    private Integer day ;
    @ExcelProperty(index = 1)
    private Double damsiteTemperature ;
    @ExcelProperty(index = 2)
    private Double s35Temperature ;
}
