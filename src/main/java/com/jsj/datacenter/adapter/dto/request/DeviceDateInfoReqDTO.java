package com.jsj.datacenter.adapter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Data
public class DeviceDateInfoReqDTO {
    @Schema(description = "设备编号")
    private String sn;
    @Schema(description = "开始时间", example = "2025-05-01 00:00:00")
    private String startTime;
    @Schema(description = "结束时间", example = "2025-05-25 00:00:00")
    private String endTime;
}
