package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AlongWayWaterTem {

    @ExcelProperty(index = 0, value = "无") // 空列占位
    private String emptyColumn;

    @ExcelProperty("断面累距")
    private Double distance;
    @ExcelProperty("水温")
    private Double waterTem;
    @ExcelProperty("气温")
    private Double airTem;
}
