package com.jsj.datacenter.application.temprature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.temprature.domain.TemperatureErrorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TemperatureErrorLogMapper extends BaseMapper<TemperatureErrorLog> {

    @Select("select * from temperature_error_log where sn = #{sn}")
    List<TemperatureErrorLog> selectBySn(String sn);
}
