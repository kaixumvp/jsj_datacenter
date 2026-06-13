package com.jsj.datacenter.infrastructure.vo;

import com.jsj.datacenter.adapter.annotation.ExcelColumn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/27
 */
@Data
public class WaterQualityItemVO {
    @ExcelColumn(name = "水源类型（不可修改）")
    @Schema(description = "水源类型（不可修改）")
    private String originType;

    @ExcelColumn(name = "点位（不可修改）")
    @Schema(description = "点位（不可修改）")
    private String name;

    @ExcelColumn(name = "监测结果")
    @Schema(description = "监测结果")
    private String level;

    @ExcelColumn(name = "点位坐标（经度）")
    @Schema(description = "点位坐标（经度）")
    private String lot;

    @ExcelColumn(name = "点位坐标（纬度）")
    @Schema(description = "点位坐标（纬度）")
    private String lat;

    @ExcelColumn(name = "自定义列1")
    @Schema(description = "自定义列1")
    private String custom1;

    @ExcelColumn(name = "自定义列2")
    @Schema(description = "自定义列2")
    private String custom2;

    @ExcelColumn(name = "自定义列3")
    @Schema(description = "自定义列3")
    private String custom3;
}
