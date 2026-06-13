package com.jsj.datacenter.infrastructure.common.interceptor;

import com.alibaba.fastjson2.JSONObject;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2024/8/2
 */
public class OkHttpRetryInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(OkHttpRetryInterceptor.class);

    /**
     * 请求返回需要重试的Code列表
     */
    static final int[] RETRY_CODE_ARRAY = {400, 403, 404, 408, 500, 502};

    /**
     * 最大重试次数
     */
    private final int maxRetries;

    /**
     * 重试间隔毫秒数
     */
    private final int retryDelayMillis;

    public OkHttpRetryInterceptor(final int maxRetries, final int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public Response intercept(Chain chain) {
        return retry(chain, 0);
    }

    /**
     * 重试
     * <p>
     * 递归处理
     *
     * @param chain
     * @param retryCent
     * @return okhttp3.Response
     */
    private Response retry(Chain chain, int retryCent) {
        Request request = chain.request();
        Response response = null;
        try {
            log.info(">>> OkHttp第{}次调用接口{}，详细请求:{}", (retryCent + 1), request.url(), request.toString());
            response = chain.proceed(request);
            int code = response.code();

            // 请求HTTP失败
            if (Arrays.asList(RETRY_CODE_ARRAY).contains(code) && maxRetries > retryCent) {
                TimeUnit.MILLISECONDS.sleep(retryDelayMillis);
                return retry(chain, retryCent + 1);
            }

            // 因为response.body().string()会关闭资源，所以生成一个快照获取ResponseBody
            ResponseBody responseBodyClone = response.peekBody(Long.MAX_VALUE);
            // 获取响应码
            String responseString = responseBodyClone.string();
            responseBodyClone.close();
            JSONObject jsonObject = JSONObject.parseObject(responseString);

            // 请求业务系统返回异常
            if ((code < 200 || code > 299) && maxRetries > retryCent) {
                TimeUnit.MILLISECONDS.sleep(retryDelayMillis);
                return retry(chain, retryCent + 1);
            }
            return response;
        } catch (Exception e) {
            if (maxRetries > retryCent) {
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    log.error(">>> OkHttp重试异常", ie);
                }
                return retry(chain, retryCent + 1);
            } else {
                return response;
            }
        }
    }
}

