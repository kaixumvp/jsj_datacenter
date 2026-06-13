package com.jsj.datacenter.application.scheduler;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.dto.request.DeviceDateInfoReqDTO;
import com.jsj.datacenter.adapter.dto.response.TemperatureErrorLogDTO;
import com.jsj.datacenter.application.DataService;
import com.jsj.datacenter.application.temprature.service.TemperatureErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TemperatureScheduler {
    @Autowired
    DataService dataService;
    @Autowired
    TemperatureErrorLogService temperatureErrorLogService;

    public TemperatureErrorLogDTO getYesterdayTemperatureInfo(String sn) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = sdf.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        DeviceDateInfoReqDTO reqDTO = new DeviceDateInfoReqDTO();
        reqDTO.setSn(sn);
        reqDTO.setStartTime(yesterday + " 00:00:00");
        reqDTO.setEndTime(yesterday + " 23:59:59");
        JSONArray historyInfos = dataService.getDateInfo(reqDTO);
        if (historyInfos == null || historyInfos.isEmpty()) {
            return null;
        }
        List<Double> temps = new ArrayList<>();

        // 收集每个通道的所有温度值
        // 遍历JSON数组处理每个数据点
        for (int i = 0; i < historyInfos.size(); i++) {
            JSONObject data = historyInfos.getJSONObject(i);
            JSONArray channels = data.getJSONArray("channelList");
            List<Double> oneDayTemp = new ArrayList<>();
            for (int j = 0; j < channels.size(); j++) {
                JSONObject channel = channels.getJSONObject(j);
                Double value = channel.getDouble("value");
                if (value != null && value < 99) {
                    oneDayTemp.add(value);
                }
            }
            double avg = oneDayTemp.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (avg > 0) {
                temps.add(avg);
            }
        }
        // 计算最大温度、最小温度和最大温差
        double maxTemp = temps.stream().max(Double::compare).orElse(0.0);
        double minTemp = temps.stream().min(Double::compare).orElse(0.0);
        double tempDiff = new BigDecimal(maxTemp + "").subtract(new BigDecimal(minTemp + "")).doubleValue();

        TemperatureErrorLogDTO result = new TemperatureErrorLogDTO();
        result.setMaxTemp(maxTemp);
        result.setMinTemp(minTemp);
        result.setTempDiff(tempDiff);
        result.setSn(sn);
        result.setDate(yesterday);

        return result;
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void checkDailyTemperatureAnomalies() {
        log.info("开始执行每日温度异常检测任务");
        try {
            // 获取所有设备SN列表（假设有方法获取所有设备）
            List<String> deviceSnList = getAllDeviceSn();

            for (String sn : deviceSnList) {
                TemperatureErrorLogDTO temp = getYesterdayTemperatureInfo(sn);

                if (temp.getTempDiff() > 5) {
                    log.warn("设备[{}]温度异常，最大温差: {}℃",
                            sn,  temp.getTempDiff());
                    // 这里可以添加异常记录逻辑，如保存到数据库
                    temperatureErrorLogService.addErrorLog(temp);
                }

            }
        } catch (Exception e) {
            log.error("温度异常检测任务执行失败", e);
        }
    }

    private List<String> getAllDeviceSn() {
        // 实现获取所有设备SN的逻辑
        JSONArray deviceInfo = dataService.getDeviceInfo("");
        if (deviceInfo != null && !deviceInfo.isEmpty()) {
            List<String> deviceSnList = new ArrayList<>();
            for (int i = 0; i < deviceInfo.size(); i++) {
                JSONObject item = deviceInfo.getJSONObject(i);
                String sn = item.getString("SN");
                deviceSnList.add(sn);
            }
            return deviceSnList;
        }
        return Collections.emptyList();
    }
}
