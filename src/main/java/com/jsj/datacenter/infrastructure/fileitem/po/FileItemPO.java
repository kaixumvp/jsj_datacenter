package com.jsj.datacenter.infrastructure.fileitem.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/25
 */
@Data
@TableName("file_items")
@Accessors(chain = true)
public class FileItemPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileKey;
    private UploadFileType fileType;
    private String filename;
    private String extension;
    private String path;
    private String urlPath;
    private Integer deleted;
    private Date createTime;
    private String createBy;
}
