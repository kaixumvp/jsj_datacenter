package com.jsj.datacenter.application;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.dto.request.DeviceDateInfoReqDTO;
import com.jsj.datacenter.adapter.dto.response.TemperatureErrorLogDTO;
import com.jsj.datacenter.application.temprature.service.TemperatureErrorLogService;
import com.jsj.datacenter.infrastructure.common.utils.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Service
@Slf4j
public class DataService {

    @Value("${datacenter.host}")
    private String apiHost;
    @Value("${datacenter.user}")
    private String username;
    @Value("${datacenter.secret}")
    private String secret;
    @Autowired
    TemperatureErrorLogService temperatureErrorLogService;

    public JSONArray getDateInfo(DeviceDateInfoReqDTO reqDTO) {
        String urlFmt = apiHost + "/api/Monitor/GetDevData?devSN=%s&startTime=%s&endTime=%s";
        String url = String.format(urlFmt, reqDTO.getSn(), reqDTO.getStartTime(), reqDTO.getEndTime());
        return JSONObject.parseObject(doRequest(url)).getJSONArray("data");
    }

    public JSONArray getDeviceInfo(String sn) {
        String urlFmt = apiHost + "/api//Monitor/ListDevStatusBySN?devSN=%s";
        String url = String.format(urlFmt, sn);
        return JSONObject.parseObject(doRequest(url)).getJSONArray("data");
    }

    private String doRequest(String url) {
        //发送GET请求 详情请见GET请求实例
        try {
            return OkHttpUtil.get(url, getHeaders(), true);
        } catch (IOException e) {
            log.error("请求失败：{}", e.getMessage());
            return "{\"data\": null}";
        }
    }

    private Map<String, String> getHeaders() {
        String date = getGMTTime();
        String signature = signature(secret, username, date);
        //身份验证信息拼接
        String authorization = "{\"UserName\":\"" + username + "\",\"Date\":\"" + date + "\",\"Signature\":\"" + signature + "\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authorization);
        return headers;
    }

    private String signature(String secret, String username, String date) {
        String txt = "\"UserName\":\"" + username + "\",\"Date\":\"" + date + "\"";
        //详情请见HMACSHA加密实例
        return hashText(txt, secret);
    }

    //获取GMT时间 详情请见获取GMT时间实例
    private String getGMTTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private String hashText(String encryptText, String encryptKey) {
        String encodeToString = "";
        try {
            byte[] data;
            data = encryptKey.getBytes("utf-8");
            SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance("HmacSHA1");
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] text = encryptText.getBytes("utf-8");
            //Mac 操作
            byte[] digest = mac.doFinal(text);
            //base64编码
            encodeToString = Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            log.error("加密失败：{}", e.getMessage());
        }
        return encodeToString;
    }

    public List<TemperatureErrorLogDTO> getTemperatureErrorLogs(String sn) {
        return temperatureErrorLogService.getErrorLogBySn(sn);
    }
}