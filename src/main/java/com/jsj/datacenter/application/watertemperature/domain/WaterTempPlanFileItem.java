package com.jsj.datacenter.application.watertemperature.domain;

import lombok.Data;

@Data
public class WaterTempPlanFileItem {
    private Integer planId;
    private Integer fileType;
    private String fileKey;
    private Integer planType;
    private String path;
    private String urlPath;
}
