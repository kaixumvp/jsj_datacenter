package com.jsj.datacenter.infrastructure.vo.annotaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WaDate {
    String pattern() default "";
}
