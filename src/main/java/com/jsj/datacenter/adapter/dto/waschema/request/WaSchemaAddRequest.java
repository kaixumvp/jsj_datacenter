package com.jsj.datacenter.adapter.dto.waschema.request;

import lombok.Data;

import java.util.List;

@Data
public class WaSchemaAddRequest {
    private String name;

    private List<SchemaParam> param;

    private String type;
}
