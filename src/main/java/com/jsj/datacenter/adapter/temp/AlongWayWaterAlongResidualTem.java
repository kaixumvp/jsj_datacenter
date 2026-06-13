package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AlongWayWaterAlongResidualTem {

    @ExcelProperty(index = 0) // 空列占位
    private Double day;
    @ExcelProperty(index = 1)
    private Double s1;
    @ExcelProperty(index = 2)
    private Double s2;
}
