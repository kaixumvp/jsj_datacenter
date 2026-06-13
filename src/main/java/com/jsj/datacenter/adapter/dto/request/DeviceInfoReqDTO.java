package com.jsj.datacenter.adapter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Data
public class DeviceInfoReqDTO {
    @Schema(description = "设备编号")
    private String sn;

    public String getSn() {
        if (sn == null) {
            return "";
        }
        return sn;
    }
}
