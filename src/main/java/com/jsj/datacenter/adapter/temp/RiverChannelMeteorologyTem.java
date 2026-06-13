package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RiverChannelMeteorologyTem {
    @ExcelProperty("时间序号")
    private double dataIndex;
    @ExcelProperty("降雨（mm）")
    private double rainfall;
    @ExcelProperty("气温（℃）")
    private double temp;
    @ExcelProperty("相对湿度（%）")
    private double relativeHumidity;
    @ExcelProperty("风速（m/s）")
    private double windSpeed;
    @ExcelProperty("风向（°）")
    private double windDirection;
    @ExcelProperty("云量")
    private double cloudAmount;
    @ExcelProperty("太阳辐射")
    private double solarRadiation;
    @ExcelProperty("水温")
    private double waterTemp;
    @ExcelProperty("表层冰厚")
    private double surfaceIce;
    @ExcelProperty("水内冰厚")
    private double innerIce;
    @ExcelProperty("表层冰厚度")
    private double surfaceIceThickness;
    @ExcelProperty("水内冰密度")
    private double innerIceDensity;
}
