package com.jsj.datacenter.infrastructure.common.config;

import com.jsj.datacenter.infrastructure.common.interceptor.OkHttpRetryInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2024/8/2
 */
@Configuration
public class OkHttpConfiguration {

    /**
     * 默认超时时间
     */
    static final long DEFAULT_TIME_OUT = 600L;

    /**
     * 默认超时时间单位
     */
    static final TimeUnit DEFAULT_TIME_OUT_UNIT = TimeUnit.SECONDS;

    /**
     * 默认最大重试次数
     */
    static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * 默认重试间隔毫秒数
     */
    static final int DEFAULT_RETRY_DELAY_MILLIS = 0;

    /**
     * 创建OkHttpClient
     * <p>
     * 超时时间：默认配置
     * 重试：否
     *
     * @param
     * @return okhttp3.OkHttpClient
     */
    public static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .readTimeout(DEFAULT_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .build();
    }

    /**
     * 创建OkHttpClient
     * <p>
     * 超时时间：根据入参自定义
     * 重试：否
     *
     * @param timeOut     超时时间
     * @param timeOutUnit 超时时间单位
     * @return okhttp3.OkHttpClient
     */
    public static OkHttpClient createClient(final Long timeOut, final TimeUnit timeOutUnit) {
        return new OkHttpClient.Builder()
                .connectTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .readTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .build();
    }

    /**
     * 创建OkHttpClient
     * <p>
     * 超时时间：默认配置
     * 重试：是
     * 重试配置：默认超时配置
     *
     * @param
     * @return okhttp3.OkHttpClient
     */
    public static OkHttpClient createClientWithRetry() {
        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .readTimeout(DEFAULT_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .addInterceptor(new OkHttpRetryInterceptor(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MILLIS))
                .build();
    }

    /**
     * 创建OkHttpClient
     * <p>
     * 超时时间：根据入参自定义
     * 重试：是
     * 重试配置：默认超时配置
     *
     * @param timeOut     超时时间
     * @param timeOutUnit 超时时间单位
     * @return okhttp3.OkHttpClient
     */
    public static OkHttpClient createClientWithRetry(final Long timeOut, final TimeUnit timeOutUnit) {
        return new OkHttpClient.Builder()
                .connectTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .readTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .addInterceptor(new OkHttpRetryInterceptor(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MILLIS))
                .build();
    }

    /**
     * 创建OkHttpClient
     * <p>
     * 超时时间：根据入参自定义
     * 重试：是
     * 重试配置：根据入参自定义
     *
     * @param timeOut          超时时间
     * @param timeOutUnit      超时时间单位
     * @param maxRetries       最大重试次数
     * @param retryDelayMillis 重试间隔毫秒数
     * @return okhttp3.OkHttpClient
     */
    public static OkHttpClient createClientWithRetry(final Long timeOut, final TimeUnit timeOutUnit,
                                                     final Integer maxRetries, final Integer retryDelayMillis) {
        return new OkHttpClient.Builder()
                .connectTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .readTimeout(null == timeOut ? DEFAULT_TIME_OUT : timeOut,
                        null == timeOutUnit ? DEFAULT_TIME_OUT_UNIT : timeOutUnit)
                .addInterceptor(new OkHttpRetryInterceptor(null == maxRetries ? DEFAULT_MAX_RETRIES : maxRetries,
                        null == retryDelayMillis ? DEFAULT_RETRY_DELAY_MILLIS : retryDelayMillis))
                .build();
    }

}
