package com.jsj.datacenter.adapter.dto.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DeviceDataReqDTO
 *
 * @description: 设备数据同步请求DTO
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @version: 1.0
 */
@Data
@Schema(description = "设备数据同步请求")
public class DeviceDataReqDTO {

    @Schema(description = "客户端ID", example = "2604160201")
    private Long clientid;

    @Schema(description = "设备编号", example = "2604160201")
    private String device_no;

    @Schema(description = "设备数据")
    private DeviceDataDetail device_data;

    /**
     * 设备数据详情
     */
    @Data
    @Schema(description = "设备数据详情")
    public static class DeviceDataDetail {

        @Schema(description = "时间戳", example = "1780625880")
        private Long timestamp;

        @Schema(description = "氮氧化物浓度(mg/m³)", example = "43")
        private Double nox;

        @Schema(description = "一氧化碳浓度(mg/m³)", example = "80.2")
        private Double co;

        @Schema(description = "氯化氢浓度(mg/m³)", example = "6.8")
        private Double hcl;

        @Schema(description = "二氧化硫浓度(mg/m³)", example = "0.2")
        private Double so2;

        @Schema(description = "粉尘浓度(mg/m³)", example = "2.4")
        private Double dust;
    }
}
