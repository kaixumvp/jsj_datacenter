package com.jsj.datacenter.application.temprature.service;

import com.jsj.datacenter.adapter.dto.response.TemperatureErrorLogDTO;
import com.jsj.datacenter.application.temprature.domain.TemperatureErrorLog;
import com.jsj.datacenter.application.temprature.mapper.TemperatureErrorLogMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TemperatureErrorLogService {
    @Autowired
    private TemperatureErrorLogMapper temperatureErrorLogMapper;

    //添加一条错误日志
    public void addErrorLog(TemperatureErrorLogDTO temperature){
        TemperatureErrorLog errorLog = new TemperatureErrorLog();
        BeanUtils.copyProperties(temperature, errorLog);
        temperatureErrorLogMapper.insert(errorLog);
    }
    //根据sn查询错误日志
    public List<TemperatureErrorLogDTO> getErrorLogBySn(String sn){
        List<TemperatureErrorLogDTO> result = new ArrayList<>();
        List<TemperatureErrorLog> errorLogs = temperatureErrorLogMapper.selectBySn(sn);
        for (TemperatureErrorLog temperatureErrorLog : errorLogs) {
            TemperatureErrorLogDTO errorLogDTO = new TemperatureErrorLogDTO();
            BeanUtils.copyProperties(temperatureErrorLog, errorLogDTO);
            result.add(errorLogDTO);
        }
        return result;
    }
}
