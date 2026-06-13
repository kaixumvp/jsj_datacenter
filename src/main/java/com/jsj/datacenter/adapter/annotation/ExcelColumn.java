package com.jsj.datacenter.adapter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    /**
     * Excel表头名称
     */
    String name();

    /**
     * 是否必填（默认false）
     */
    boolean required() default false;

    /**
     * 日期格式（仅对日期类型有效）
     */
    String dateFormat() default "yyyy-MM-dd";

    // 数字格式化参数
    String numberFormat() default "";
}
