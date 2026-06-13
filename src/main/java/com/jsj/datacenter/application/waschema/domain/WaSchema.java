package com.jsj.datacenter.application.waschema.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("wa_schema")
@Data
/**
 * 评估方案
 */
public class WaSchema {

    private Integer id;

    private String name;

    private String type;

    private String createTime;

    private String createBy;
}
