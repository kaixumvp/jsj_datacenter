package com.jsj.datacenter.application.oversaturation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OverSaturationMapper extends BaseMapper<OverSaturation> {

    @Select(value = "select * from over_saturation where period = #{period} limit 1")
    OverSaturation getByPeriod(String period);

    @Update(value = "update over_saturation set file_key = #{fileKey}, data = #{data} where period = #{period}")
    void updateByPeriod(OverSaturation waterQuality);

    @Delete(value = "delete from over_saturation where period = #{period}")
    void removeByPeriod(String period);

    @Select(value = "select period from over_saturation order by create_time desc")
    List<String> getByPeriods();

    @Select(value = "select * from over_saturation")
    @Results({
            @Result(property = "period", column = "period"),
            @Result(property = "fileKey", column = "file_key"),
            @Result(property = "overSaturationPoints", column = "period",
                    many = @Many(select = "com.jsj.datacenter.application.oversaturation.mapper.OverSaturationPointMapper.selectByPeriod"))
    })
    List<OverSaturation> selectAllOverSaturationsWithOverSaturationPoints();
}
