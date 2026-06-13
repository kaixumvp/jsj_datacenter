package com.jsj.datacenter.adapter.dto.response;

import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import lombok.Data;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/25
 */
@Data
public class FileItemDTO {
    private int records;
    private String fileKey;
    private UploadFileType fileType;
    private String filename;
    private String extension;
    private String createTime;
    private String path;
    private String urlPath;
}
