package com.jsj.datacenter.adapter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.adapter.entity.DeviceData;
import org.apache.ibatis.annotations.Mapper;

/**
 * DeviceDataMapper
 *
 * @description: 设备数据Mapper接口
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @version: 1.0
 */
@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {
}