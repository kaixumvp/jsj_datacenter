package com.jsj.datacenter.adapter.dto.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeviceDataRespDTO
 *
 * @description: 设备数据同步响应DTO
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备数据同步响应")
public class DeviceDataRespDTO {

    @Schema(description = "处理结果", example = "success")
    private String result;

    @Schema(description = "消息", example = "数据接收成功")
    private String message;

    @Schema(description = "设备编号")
    private String device_no;

    @Schema(description = "接收时间")
    private String receive_time;
}
