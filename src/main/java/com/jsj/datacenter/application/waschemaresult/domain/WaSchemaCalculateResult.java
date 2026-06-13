package com.jsj.datacenter.application.waschemaresult.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wa_schema_calculate_result")
/**
 * 评估方案计算结果
 */
public class WaSchemaCalculateResult {

    private Integer id;
    private Integer schemaId;

    private String status;

    private String completeRate;

    private String resultFileKey;

    private String resultFileData;

    private String logId;

    private String createBy;

    private String createTime;

    private String traceId;
}
