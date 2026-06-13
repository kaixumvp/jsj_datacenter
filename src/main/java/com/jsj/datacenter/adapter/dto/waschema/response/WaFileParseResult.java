package com.jsj.datacenter.adapter.dto.waschema.response;

import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import lombok.Data;

@Data
public class WaFileParseResult {

    private String fileKey;
    private String filename;
    private UploadFileType fileType;
    private Object data;
    private String startTime;
    private String endTime;
    private String duration;
}
