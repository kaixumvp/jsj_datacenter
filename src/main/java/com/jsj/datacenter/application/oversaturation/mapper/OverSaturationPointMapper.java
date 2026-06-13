package com.jsj.datacenter.application.oversaturation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturation;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturationPoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OverSaturationPointMapper extends BaseMapper<OverSaturationPoint> {

    @Delete(value = "delete from over_saturation_point where period = #{period}")
    void deleteByPeriod(String period);

    @Select(value = "select * from over_saturation_point where period = #{period}")
    List<OverSaturationPoint> selectByPeriod(String period);

}
