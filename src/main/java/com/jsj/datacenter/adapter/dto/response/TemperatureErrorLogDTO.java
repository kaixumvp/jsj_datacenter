package com.jsj.datacenter.adapter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TemperatureErrorLogDTO {

  @Schema(description = "设备序列号")
  private String sn;

  @Schema(description = "日期")
  private String date;

  @Schema(description = "最高温度")
  private Double maxTemp;

  @Schema(description = "最低温度")
  private Double minTemp;

  @Schema(description = "最大温度差")
  private Double tempDiff;
}
