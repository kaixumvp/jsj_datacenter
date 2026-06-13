package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.device.DeviceDataReqDTO;
import com.jsj.datacenter.adapter.dto.device.DeviceDataRespDTO;
import com.jsj.datacenter.adapter.service.DeviceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * DeviceController
 *
 * @description: 设备数据同步模块，用于接收和管理厂商设备数据
 * @className: DeviceController
 * @author: jiangshengjie
 * @date: 2020/6/23 11:01 下午
 * @version: 1.0
 * @since JDK 1.8
 */
@Tag(name = "设备数据管理", description = "设备数据同步相关接口")
@Controller
@RestController
@RequestMapping("/api/device")
@EasyResponse
public class DeviceController {

    @Autowired
    private DeviceDataService deviceDataService;

    /**
     * 接收设备数据
     *
     * @description: 接收厂商设备上传的监测数据，包括NOX、CO、HCL、SO2、粉尘等指标
     * @param reqDTO 设备数据请求对象
     * @return 处理结果
     * @author: ${USER}
     * @date: ${DATE} ${TIME}
     */
    @Operation(summary = "接收设备数据", description = "接收厂商设备上传的监测数据")
    @PostMapping("/data/receive")
    public DeviceDataRespDTO receiveDeviceData(@RequestBody DeviceDataReqDTO reqDTO) {
        return deviceDataService.receiveDeviceData(reqDTO);
    }

    /**
     * 健康检查接口
     *
     * @description: 用于检测设备数据接收服务是否正常运行
     * @return 服务状态
     * @author: ${USER}
     * @date: ${DATE} ${TIME}
     */
    @Operation(summary = "健康检查", description = "检查服务运行状态")
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
