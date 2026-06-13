package com.jsj.datacenter.adapter.dto.waschema.request;

import lombok.Data;

@Data
public class SchemaParam {
    private String key;

    private String duration;

    private String value;

    private String startTime;

    private String endTime;

    private String fileKey;

    private String fileData;
}
