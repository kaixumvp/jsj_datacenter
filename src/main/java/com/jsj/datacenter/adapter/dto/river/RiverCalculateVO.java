package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

import java.util.Date;

@Data
public class RiverCalculateVO {
    //WA_SCHEMA_PARAM_WEATHER("气象参数", "excel", WaSchemaWeatherItemVO.class),
    //    WA_SCHEMA_PARAM_INBOUND_TRAFFIC("入库流量参数", "excel", WaSchemaInTrafficItemVO.class),
    //    WA_SCHEMA_PARAM_OUTBOUND_TRAFFIC("出库流量参数", "excel", WaSchemaOutTrafficItemVO.class),
    //    WA_SCHEMA_PARAM_INBOUND_TEMP("入库水温", "excel", WaSchemaOutTrafficItemVO.class),
    //    WA_SCHEMA_PARAM_INITIAL_LEVEL("初始运行水位", "excel", WaSchemaOutTrafficItemVO.class)
    private Integer plan_id;                    //任务id
    private String planName;                    //方案名称
    private Integer planType;                   //方案类型
    private Date planStartTime;                 //方案开始时间
    private Date planEndTime;                   //方案结束时间

    //河道模型参数
    private String hdWeatherFileKey;                    //气象文件key
    private String mainStreamInflowFileKey;             //干流流量文件key
    private String mainStreamTemperatureFileKey;        //干流水温文件key
    private String branchStreamInflowFileKey;           //支流流量文件key
    private String branchStreamTemperatureFileKey;      //支流水温文件key
    private String hdComparisonFileKey;
    //水库模型参数
    private String weatherFileKey;              //气象文件key
    private String inboundTrafficFileKey;       //入库流量文件key
    private String outboundTrafficFileKey;      //出库流量文件key
    private String inboundTempFileKey;          //入库水温文件key
    private String comparisonFileKey;           //对比数据文件key
    private Double initialLevel;               //初始运行水位文件key
    //水库模型预设参数
    private Double exh2O;
    private Double beta;
    private Double spread;                      //扩散
    //河道模型预设参数
    private Double solarcoe;
    private Double mecovertcoe;
    private Double windcoe;

    private Double initialField;

    private Integer totalDay;  //总天数
}
