package com.jsj.datacenter.infrastructure.common.utils;

import org.slf4j.MDC;

import java.util.UUID;

public class TraceUtil {
    private static final String TRACE_ID_KEY = "traceId";
    
    // 生成traceId并放入MDC
    public static void putTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }
    
    // 从MDC获取traceId
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    // 清除traceId
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}