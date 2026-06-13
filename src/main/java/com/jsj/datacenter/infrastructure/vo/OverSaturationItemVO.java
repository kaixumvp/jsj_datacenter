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
public class OverSaturationItemVO {
    @ExcelColumn(name = "监测项目（不可修改）")
    @Schema(description = "监测项目（不可修改）")
    private String name;

    @ExcelColumn(name = "单位（不可修改）")
    @Schema(description = "单位（不可修改）")
    private String unit;

    @ExcelColumn(name = "点位坐标（经度）")
    @Schema(description = "点位坐标（经度）")
    private String longitude;

    @ExcelColumn(name = "点位坐标（纬度）")
    @Schema(description = "点位坐标（纬度）")
    private String latitude;

    @ExcelColumn(name = "监测结果（数值）")
    @Schema(description = "监测结果（数值）")
    private String value;

    @ExcelColumn(name = "备注（非必填）")
    @Schema(description = "备注（非必填）")
    private String remark;
}
