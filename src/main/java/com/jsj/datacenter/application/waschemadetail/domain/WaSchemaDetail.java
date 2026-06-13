package com.jsj.datacenter.application.waschemadetail.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wa_schema_detail")
/**
 * 评估方案详情表
 */
public class WaSchemaDetail {
    private Integer id;

    private Integer schemaId;

    private String key;

    private String value;

    private String fileKey;

    private String fileData;

    private String startTime;

    private String endTime;

    private String duration;

    private String createTime;

    private String createBy;
}
