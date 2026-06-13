package com.jsj.datacenter.application.watertemperature.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PlanParamsResult {
    private Integer temperature;
    private String planName;
    private Integer planType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndTime;

    private Double exh;
    private Double beta;
    private Double spread;                      //扩散

    //河道模型预设参数
    private Double solarcoe;
    private Double mecovertcoe;
    private Double windcoe;

    private Double initialField;

    private String weatherFileKey;              //气象文件key
    private String weatherFileName;             //气象文件name
    private String inboundTrafficFileKey;       //入库流量文件key
    private String inboundTrafficFileName;       //入库流量文件name
    private String outboundTrafficFileKey;      //出库流量文件key
    private String outboundTrafficFileName;      //出库流量文件name
    private String inboundTempFileKey;          //入库水温文件key
    private String inboundTempFileName;          //入库水温文件key
    private String comparisonFileKey;           //对比数据文件key
    private String comparisonFileName;           //对比数据文件key
    private Double initialLevel;               //初始运行水位文件key


    //河道模型参数
    private String hdWeatherFileKey;                    //气象文件key
    private String hdWeatherFileName;                    //气象文件Name
    private String mainStreamInflowFileKey;             //干流流量文件key
    private String mainStreamInflowFileName;             //干流流量文件Name
    private String mainStreamTemperatureFileKey;        //干流水温文件key
    private String mainStreamTemperatureFileName;        //干流水温文件Name
    private String branchStreamInflowFileKey;           //支流流量文件key
    private String branchStreamInflowFileName;           //支流流量文件Name
    private String branchStreamTemperatureFileKey;      //支流水温文件key
    private String branchStreamTemperatureFileName;      //支流水温文件Name
}

