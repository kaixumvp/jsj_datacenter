package com.jsj.datacenter.adapter.service;

import com.jsj.datacenter.adapter.dto.device.DeviceDataReqDTO;
import com.jsj.datacenter.adapter.dto.device.DeviceDataRespDTO;
import com.jsj.datacenter.adapter.entity.DeviceData;
import com.jsj.datacenter.adapter.mapper.DeviceDataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceDataServiceTest {

    @Mock
    private DeviceDataMapper deviceDataMapper;

    @InjectMocks
    private DeviceDataService deviceDataService;

    private DeviceDataReqDTO validReqDTO;

    @BeforeEach
    void setUp() {
        validReqDTO = new DeviceDataReqDTO();
        validReqDTO.setDevice_no("DEV-001");
        validReqDTO.setClientid(123L);

        DeviceDataReqDTO.DeviceDataDetail detail = new DeviceDataReqDTO.DeviceDataDetail();
        detail.setTimestamp(1718071200L); // 2025-06-11 10:00:00 的时间戳
        detail.setNox(10.5);
        detail.setCo(5.2);
        detail.setHcl(1.0);
        detail.setSo2(8.3);
        detail.setDust(15.0);
        validReqDTO.setDevice_data(detail);
    }

    @Test
    void receiveDeviceData_withValidDataAndSaveSuccess_returnsSuccess() {
        when(deviceDataMapper.insert(any(DeviceData.class))).thenReturn(1);

        DeviceDataRespDTO response = deviceDataService.receiveDeviceData(validReqDTO);

        assertNotNull(response);
        assertEquals("success", response.getResult());
        assertEquals("数据接收成功", response.getMessage());
        assertEquals("DEV-001", response.getDevice_no());
        assertNotNull(response.getReceive_time());

        verify(deviceDataMapper).insert(any(DeviceData.class));
    }

    @Test
    void receiveDeviceData_withValidDataAndSaveFailure_returnsFail() {
        when(deviceDataMapper.insert(any(DeviceData.class))).thenReturn(0);

        DeviceDataRespDTO response = deviceDataService.receiveDeviceData(validReqDTO);

        assertNotNull(response);
        assertEquals("fail", response.getResult());
        assertEquals("数据保存失败", response.getMessage());
        assertEquals("DEV-001", response.getDevice_no());
    }

    @Test
    void receiveDeviceData_withNullDeviceDataAndSaveSuccess_savesSuccessfully() {
        DeviceDataReqDTO reqDTO = new DeviceDataReqDTO();
        reqDTO.setDevice_no("DEV-002");
        reqDTO.setClientid(456L);
        reqDTO.setDevice_data(null);

        when(deviceDataMapper.insert(any(DeviceData.class))).thenReturn(1);

        DeviceDataRespDTO response = deviceDataService.receiveDeviceData(reqDTO);

        assertNotNull(response);
        assertEquals("success", response.getResult());
        assertEquals("数据接收成功", response.getMessage());
    }

    @Test
    void receiveDeviceData_withException_returnsError() {
        when(deviceDataMapper.insert(any(DeviceData.class))).thenThrow(new RuntimeException("DB connection error"));

        DeviceDataRespDTO response = deviceDataService.receiveDeviceData(validReqDTO);

        assertNotNull(response);
        assertEquals("error", response.getResult());
        assertTrue(response.getMessage().contains("数据处理异常"));
        assertTrue(response.getMessage().contains("DB connection error"));
        assertEquals("DEV-001", response.getDevice_no());
    }

    @Test
    void receiveDeviceData_entityHasCorrectFields() {
        when(deviceDataMapper.insert(any(DeviceData.class))).thenReturn(1);

        deviceDataService.receiveDeviceData(validReqDTO);

        verify(deviceDataMapper).insert(argThat(deviceData -> {
            assertEquals("DEV-001", deviceData.getDeviceNo());
            assertEquals(Long.valueOf(123L), deviceData.getClientid());
            assertEquals(Double.valueOf(10.5), deviceData.getNox());
            assertEquals(Double.valueOf(5.2), deviceData.getCo());
            assertEquals(Double.valueOf(8.3), deviceData.getSo2());
            assertEquals(Double.valueOf(15.0), deviceData.getDust());
            return true;
        }));
    }
}
