package com.jsj.datacenter.application.envmeasures.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jsj.datacenter.infrastructure.common.enums.EvnMeasureType;
import lombok.Data;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Data
@TableName("env_measures")
public class EnvMeasures {

    @TableId(type = IdType.AUTO)
    private Long id;

    private EvnMeasureType eventType;

    private String title;

    private Integer weigh;

    private String content;

    private Integer implCondition;

    private String process;

    private String createTime;

    private String updateTime;

    private String createBy;

    private String updateBy;

}
