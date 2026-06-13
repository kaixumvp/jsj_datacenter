package com.jsj.datacenter.adapter.dto.river;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DischargeWaterCondition {
    @ExcelProperty("Month")
    private Integer month;
    private Double naturalWaterTemp;
    @ExcelProperty("AverageTemperature")
    private Double dischargeWaterTemp;
    private Double difference;

    private Double naturalS35Temp;
}
