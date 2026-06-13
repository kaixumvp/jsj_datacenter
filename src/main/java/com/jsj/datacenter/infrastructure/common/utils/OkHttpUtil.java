package com.jsj.datacenter.infrastructure.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.infrastructure.common.config.OkHttpConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2024/8/2
 */
@Slf4j
public class OkHttpUtil {

    /**
     * 按照默认策略GET请求
     * <p>
     * 默认策略 >>> 超时时间：3
     * 默认策略 >>> 超时时间单位：TimeUnit.SECONDS
     * 默认策略 >>> 重试最大次数：2
     * 默认策略 >>> 重试间隔毫秒数：0
     *
     * @param url     请求地址
     * @param headMap 请求头
     * @param retry   是否重试
     * @return java.lang.String
     */
    public static String get(final String url, final Map<String, String> headMap, final boolean retry) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry();
        }

        // 构建请求
        Request request = buildGetRequest(url, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 自定义策略GET请求
     *
     * @param url              请求地址
     * @param headMap          请求头
     * @param timeOut          超时时间
     * @param timeOutUnit      超时时间单位
     * @param retry            是否重试
     * @param maxRetries       重试最大次数
     * @param retryDelayMillis 重试间隔毫秒数
     * @return java.lang.String
     */
    public static String get(final String url, final Map<String, String> headMap, final Long timeOut, final TimeUnit timeOutUnit,
                             final boolean retry, final Integer maxRetries, final Integer retryDelayMillis) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient(timeOut, timeOutUnit);
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry(timeOut, timeOutUnit, maxRetries, retryDelayMillis);
        }

        // 构建请求
        Request request = buildGetRequest(url, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 按照默认策略GET请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headMap 请求头
     * @param retry   是否重试
     * @return String
     */
    public static String getForm(final String url, final Map<String, Object> params, final Map<String, String> headMap, final boolean retry) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry();
        }

        // 构建请求
        Request request = buildGetRequest(url, params, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 按照默认策略POST请求
     * <p>
     * 默认策略 >>> 超时时间：3
     * 默认策略 >>> 超时时间单位：TimeUnit.SECONDS
     * 默认策略 >>> 重试最大次数：2
     * 默认策略 >>> 重试间隔毫秒数：0
     *
     * @param url     请求地址
     * @param obj     请求body
     * @param headMap 请求header
     * @param retry   是否重试
     * @return java.lang.String
     */
    public static String postJson(final String url, final Object obj, final Map<String, String> headMap,
                                  final boolean retry) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry();
        }

        // 构建请求
        Request request = buildPostJsonRequest(url, obj, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 按照默认策略POST请求
     * <p>
     * 默认策略 >>> 超时时间：3
     * 默认策略 >>> 超时时间单位：TimeUnit.SECONDS
     * 默认策略 >>> 重试最大次数：2
     * 默认策略 >>> 重试间隔毫秒数：0
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headMap 请求header
     * @param retry   是否重试
     * @return java.lang.String
     */
    public static String postForm(final String url, final Map<String, Object> params,
                                  final Map<String, String> headMap, final boolean retry) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry();
        }

        // 构建请求
        Request request = buildPostFormRequest(url, params, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 文件上传
     *
     * @param url           请求地址
     * @param fileFieldName 文件字段名
     * @param file          文件
     * @param headMap       header
     * @param params        请求参数
     * @return java.lang.String
     */
    public static String postFile(final String url, final String fileFieldName, File file,
                                  final Map<String, String> headMap, final Map<String, Object> params) throws IOException {
        String fileName = fileFieldName;
        if (StringUtils.isEmpty(fileName)) {
            fileName = "file";
        }
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileName, file.getName(), fileBody);
        if (ObjectUtils.isNotEmpty(params)) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, (String) params.get(key));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(headMap))
                .post(builder.build())
                .build();
        return execute(request, okHttpClient);
    }

    /**
     * 自定义策略POST请求
     *
     * @param url              请求地址
     * @param obj              请求body
     * @param headMap          请求header
     * @param timeOut          超时时间
     * @param timeOutUnit      超时时间单位
     * @param retry            是否重试
     * @param maxRetries       重试最大次数
     * @param retryDelayMillis 重试间隔毫秒数
     * @return java.lang.String
     */
    public static String postJson(final String url, final Object obj, final Map<String, String> headMap,
                                  final Long timeOut, final TimeUnit timeOutUnit, final boolean retry,
                                  final Integer maxRetries, final Integer retryDelayMillis) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient(timeOut, timeOutUnit);
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry(timeOut, timeOutUnit, maxRetries, retryDelayMillis);
        }

        // 构建请求
        Request request = buildPostJsonRequest(url, obj, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 自定义策略POST请求
     *
     * @param url              请求地址
     * @param params           请求参数
     * @param headMap          请求header
     * @param timeOut          超时时间
     * @param timeOutUnit      超时时间单位
     * @param retry            是否重试
     * @param maxRetries       重试最大次数
     * @param retryDelayMillis 重试间隔毫秒数
     * @return java.lang.String
     */
    public static String postForm(final String url, final Map<String, Object> params, final Map<String, String> headMap,
                                  final Long timeOut, final TimeUnit timeOutUnit, final boolean retry, final Integer maxRetries,
                                  final Integer retryDelayMillis) throws IOException {
        // OkHttp配置项初始化
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient(timeOut, timeOutUnit);
        if (retry) {
            okHttpClient = OkHttpConfiguration.createClientWithRetry(timeOut, timeOutUnit, maxRetries, retryDelayMillis);
        }

        // 构建请求
        Request request = buildPostFormRequest(url, params, headMap);

        // 提交请求，返回结果
        return execute(request, okHttpClient);
    }

    /**
     * 将url下载至file
     *
     * @param url  url
     * @param file file
     */
    public static void urlToFile(String url, File file) {
        if (null == file) {
            return;
        }
        if (file.exists()) {
            file.delete();
        }
        // 创建父级目录
        File parentFile = file.getParentFile();
        if (null != parentFile && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        OkHttpClient okHttpClient = OkHttpConfiguration.createClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                try (InputStream inputStream = response.body().byteStream()) {
                    Files.copy(inputStream, file.toPath());
                }
            }
        } catch (IOException e) {
            log.error("[urlToFile] error", e);
        }
    }

    /**
     * 提交请求，返回结果
     *
     * @param request
     * @param okHttpClient
     * @return java.lang.String
     */
    private static String execute(final Request request, final OkHttpClient okHttpClient) throws IOException {
        Instant start = Instant.now();
        Response response = okHttpClient.newCall(request).execute();
        Instant end = Instant.now();
        log.info(">>> OkHttp调用时间统计 >>> 接口：{}，执行时间：{}ms", request.url(), end.toEpochMilli() - start.toEpochMilli());
        String res = "";
        if (null != response.body()) {
            res = response.body().string();
            log.info(">>> OkHttp调用响应结果：{}", res);
        }
        // 销毁资源
        response.close();
        // 关闭client
        okHttpClient.dispatcher().executorService().shutdown();
        return res;
    }

    /**
     * Request Get组装
     *
     * @param url     请求地址
     * @param headMap 请求头
     * @return okhttp3.Request
     */
    private static Request buildGetRequest(final String url, final Map<String, String> headMap) {
        Request request = new Request.Builder().url(url).build();
        if (null != headMap) {
            request = new Request.Builder().url(url).headers(buildHeaders(headMap)).build();
        }
        return request;
    }

    /**
     * Request Get组装
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headMap 请求头
     * @return okhttp3.Request
     */
    private static Request buildGetRequest(final String url, final Map<String, Object> params, final Map<String, String> headMap) {
        String fullUrl = buildUrlWithParams(url, params);
        Request request = new Request.Builder().url(fullUrl).build();
        if (null != headMap) {
            request = new Request.Builder().url(fullUrl).headers(buildHeaders(headMap)).build();
        }
        return request;
    }

    /**
     * Request PostJson组装
     *
     * @param url     请求地址
     * @param obj     请求body
     * @param headMap 请求header
     * @return okhttp3.Request
     */
    private static Request buildPostJsonRequest(final String url, final Object obj, final Map<String, String> headMap) {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(obj)))
                .addHeader("Content-Type", "application/json")
                .headers(buildHeaders(headMap))
                .build();
    }

    /**
     * Request PostForm组装
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headMap 请求header
     * @return okhttp3.Request
     */
    private static Request buildPostFormRequest(final String url, final Map<String, Object> params, final Map<String, String> headMap) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (String key : params.keySet()) {
            formBuilder.add(key, (String) params.get(key));
        }
        FormBody body = formBuilder.build();
        return new Request.Builder().url(url).headers(buildHeaders(headMap)).post(body).build();
    }

    /**
     * Headers组装
     *
     * @param headersParams header参数
     * @return 组装后的header
     */
    private static Headers buildHeaders(final Map<String, String> headersParams) {
        Headers headers;
        okhttp3.Headers.Builder headersBuilder = new okhttp3.Headers.Builder();
        if (ObjectUtils.isNotEmpty(headersParams)) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                headersBuilder.add(key, headersParams.get(key));
            }
        }
        headers = headersBuilder.build();
        return headers;
    }

    private static String buildUrlWithParams(String url, Map<String, Object> params) {
        StringBuilder fullUrl = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            fullUrl.append("?");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                fullUrl.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            fullUrl.deleteCharAt(fullUrl.length() - 1); // Remove the trailing '&'
        }
        return fullUrl.toString();
    }
}
