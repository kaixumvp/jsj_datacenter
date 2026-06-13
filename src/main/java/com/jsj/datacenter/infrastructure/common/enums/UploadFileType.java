package com.jsj.datacenter.infrastructure.common.enums;

import com.jsj.datacenter.infrastructure.vo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/25
 */
public enum UploadFileType {

    ENV_APPROVAL("环境批复", "excel", WaterQualityItemVO.class),
    ENV_EFFECT("环境影响", "excel", WaterQualityItemVO.class),
    ENV_PROTECT("环保图片", "image", null),
    IMPL_MATTERS("落实事项", "excel", WaterQualityItemVO.class),
    SATURATION_DATA("饱和度数据", "excel", WaterQualityItemVO.class),
    OVER_SATURATION_DATA("过饱和度数据", "excel", OverSaturationItemVO.class),
    SATURATION_PIC("饱和度图片", "image", null),
    WATER_QUALITY("水质数据", "excel", WaterQualityItemVO.class),
    WATER_TEMP_PIC("水温检测图片", "image", null),
    RIVER_ENGINEERING_PIC("流域工程图片", "image", null),

    WA_SCHEMA_PARAM_WEATHER("气象参数", "excel", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_INBOUND_TRAFFIC("入库流量参数", "excel", WaSchemaInTrafficItemVO.class),
    WA_SCHEMA_PARAM_OUTBOUND_TRAFFIC("出库流量参数", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_INBOUND_TEMP("入库水温", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_INITIAL_LEVEL("初始运行水位", "excel", WaSchemaOutTrafficItemVO.class),

    //水库模型计算结果
    WA_SCHEMA_PARAM_RIVER_OUT_XX("下泄水温", "png", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_XX_EXCEL("下泄水温表格","excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_YT("二维云图gif", "gif", WaSchemaInTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_DIMENSIONAL_YT("二维云图", "png", WaSchemaInTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_CXWD("垂向水温图", "png", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_CXWD_EXCEL("垂向水温", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_BQWD("坝前温度二维云图", "png", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_CXWD_DIFF("垂向水温对比图", "png", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_OUT_XX_DIFF("下泄水温对比图", "png", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RIVER_COMP("对比结果数据", "excel", WaSchemaOutTrafficItemVO.class),

    //河道模型边界条件文件
    WA_SCHEMA_PARAM_WEATHER_RESERVOIR("气象参数", "excel", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_MAIN_STREAM_INFLOW("干流流量参数", "excel", WaSchemaInTrafficItemVO.class),
    WA_SCHEMA_PARAM_MAIN_STREAM_TEMPERATURE("干流温度参数", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_BRANCH_STREAM_INFLOW("支流流量参数", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_BRANCH_STREAM_TEMPERATURE("支流温度参数", "excel", WaSchemaOutTrafficItemVO.class),
    WA_SCHEMA_PARAM_RESERVOIR_COMP("实测数据", "excel", WaSchemaOutTrafficItemVO.class),

    WA_SCHEMA_PARAM_RESER_OUT_RESULT_EXCEL("计算结果", "excel", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RESER_OUT_GIF("沿程水温", "gif", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RESER_OUT_PROFILE("沿程水温", "png", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RESER_OUT_ALONG_WAY("断面水温", "png", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_PROFILE("对比沿程水温", "png", WaSchemaWeatherItemVO.class),
    WA_SCHEMA_PARAM_RESER_OUT_DIFFRENT_ALONG_WAY("对比断面水温", "png", WaSchemaWeatherItemVO.class),

    WA_SCHEMA_PARAM_WATER_TEMPERATURE_REPORT("水温报告", "docx", WaSchemaWeatherItemVO.class),
    ;

    private final String name;
    private final String fileType;
    // todo 新增各个类型的Excel模板并修改此处配置
    private final Class<?> clazz;

    UploadFileType(String name, String fileType, Class<?> clazz) {
        this.name = name;
        this.fileType = fileType;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public String getFileType() {
        return fileType;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
