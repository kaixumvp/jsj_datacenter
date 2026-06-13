package com.jsj.datacenter.application.watertemperature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlanFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Mapper
public interface WaterTemperaturePlanFileMapper extends BaseMapper<WaterTemperaturePlanFile> {


    @Select(value = "select * from water_temperature_plan_file_items where plan_id = #{planId} AND file_type = #{fileType}")
    List<WaterTemperaturePlanFile> selectPlanFileList(@Param("planId") Integer planId, @Param("fileType") Integer fileType);

    @Select(value = "select * from water_temperature_plan_file_items where plan_id = #{planId}")
    List<WaterTemperaturePlanFile> selectPlanFileListByPlanId(@Param("planId") Integer planId);

    @Select(value = "delete FROM water_temperature_plan_file_items where plan_id = #{planId} AND file_type = #{fileType}")
    void deleteByPlanIdAndType(@Param("planId") Integer planId, @Param("fileType") Integer fileType);
}
