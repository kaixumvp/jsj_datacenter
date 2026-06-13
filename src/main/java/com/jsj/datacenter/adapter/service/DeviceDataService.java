package com.jsj.datacenter.adapter.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsj.datacenter.adapter.dto.device.DeviceDataReqDTO;
import com.jsj.datacenter.adapter.dto.device.DeviceDataRespDTO;
import com.jsj.datacenter.adapter.entity.DeviceData;
import com.jsj.datacenter.adapter.mapper.DeviceDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DeviceDataService
 *
 * @description: 设备数据服务类
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @version: 1.0
 */
@Service
@Slf4j
public class DeviceDataService extends ServiceImpl<DeviceDataMapper, DeviceData> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 接收并保存设备数据
     *
     * @param reqDTO 设备数据请求DTO
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public DeviceDataRespDTO receiveDeviceData(DeviceDataReqDTO reqDTO) {
        log.info("接收到设备数据，设备编号: {}, 客户端ID: {}", reqDTO.getDevice_no(), reqDTO.getClientid());

        try {
            DeviceData deviceData = new DeviceData();
            BeanUtils.copyProperties(reqDTO, deviceData);

            if (reqDTO.getDevice_data() != null) {
                DeviceDataReqDTO.DeviceDataDetail detail = reqDTO.getDevice_data();
                deviceData.setTimestamp(detail.getTimestamp());
                deviceData.setNox(detail.getNox());
                deviceData.setCo(detail.getCo());
                deviceData.setHcl(detail.getHcl());
                deviceData.setSo2(detail.getSo2());
                deviceData.setDust(detail.getDust());
            }

            deviceData.setCreateTime(new Date());
            deviceData.setUpdateTime(new Date());

            boolean saved = this.save(deviceData);

            if (saved) {
                log.info("设备数据保存成功，设备编号: {}", reqDTO.getDevice_no());

                return DeviceDataRespDTO.builder()
                        .result("success")
                        .message("数据接收成功")
                        .device_no(reqDTO.getDevice_no())
                        .receive_time(DATE_FORMAT.format(new Date()))
                        .build();
            } else {
                log.error("设备数据保存失败，设备编号: {}", reqDTO.getDevice_no());

                return DeviceDataRespDTO.builder()
                        .result("fail")
                        .message("数据保存失败")
                        .device_no(reqDTO.getDevice_no())
                        .receive_time(DATE_FORMAT.format(new Date()))
                        .build();
            }
        } catch (Exception e) {
            log.error("处理设备数据异常，设备编号: {}", reqDTO.getDevice_no(), e);

            return DeviceDataRespDTO.builder()
                    .result("error")
                    .message("数据处理异常: " + e.getMessage())
                    .device_no(reqDTO.getDevice_no())
                    .receive_time(DATE_FORMAT.format(new Date()))
                    .build();
        }
    }
}