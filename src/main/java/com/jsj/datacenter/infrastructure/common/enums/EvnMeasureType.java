package com.jsj.datacenter.infrastructure.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Getter
@AllArgsConstructor
public enum EvnMeasureType {
    ENV_APPROVAL("环境批复"),
    ENV_EFFECT("环境影响"),
    EXTRA("其他");

    private final String name;
}