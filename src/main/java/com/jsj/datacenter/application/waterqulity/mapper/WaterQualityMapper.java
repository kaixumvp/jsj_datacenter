package com.jsj.datacenter.application.waterqulity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.waterqulity.domain.WaterQuality;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WaterQualityMapper extends BaseMapper<WaterQuality> {

    @Select(value = "select * from water_quality where period = #{period} limit 1")
    WaterQuality getByPeriod(String period);

    @Update(value = "update water_quality set file_key = #{fileKey}, data = #{data} where period = #{period}")
    void updateByPeriod(WaterQuality waterQuality);

    @Delete(value = "delete from water_quality where period = #{period}")
    void removeByPeriod(String period);

    @Select(value = "select period from water_quality order by create_time desc")
    List<String> getByPeriods();
}
