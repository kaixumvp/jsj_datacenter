package com.jsj.datacenter.application.task;

import com.jsj.datacenter.application.waschemadetail.domain.WaSchemaDetail;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskParam {
    private String schemaId;
    private String schemaName;
    private String type;
    private List<WaSchemaDetail> waSchemaDetails = new ArrayList<>();
}
