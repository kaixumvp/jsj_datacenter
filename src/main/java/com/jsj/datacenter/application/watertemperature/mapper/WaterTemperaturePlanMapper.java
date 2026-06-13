package com.jsj.datacenter.application.watertemperature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.watertemperature.domain.WaterTempPlanFileItem;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaterTemperaturePlanMapper extends BaseMapper<WaterTemperaturePlan> {

    @Select(value = "select * from water_temperature_plan where plan_type = #{planType} limit 1")
    WaterTemperaturePlan selectListByPlanTypeOne(Integer planType);

    @Select(value = "select *from water_temperature_plan where plan_name LIKE '%${planName}%' and plan_type <> 1 and plan_type <> 2 limit #{startIndex}, #{endIndex}")
    List<WaterTemperaturePlan> selectListLikeNameLimitPage(@Param("planName") String planName,
                                                           @Param("startIndex") Integer startIndex,
                                                           @Param("endIndex") Integer endIndex);

    @Select(value = "select count(*) from water_temperature_plan where plan_name LIKE '%${planName}%' and plan_type <> 1 and plan_type <> 2")
    int selectListLikeNameCount(@Param("planName") String planName);

    @Select(value = "select count(*) from water_temperature_plan where plan_type <> 1 and plan_type <> 2")
    int totals();

    //private int totals;
    //    private int successNum;
    //    private int failNum;
    //    private int inProcessNum;
    @Select(value = "select count(*) from water_temperature_plan where progress_status = 6 and plan_type <> 1 and plan_type <> 2")
    int successNum();

    @Select(value = "select count(*) from water_temperature_plan where progress_status = -1 and plan_type <> 1 and plan_type <> 2")
    int failNum();

    @Select(value = "select count(*) from water_temperature_plan where progress_status > 0 and progress_status < 6 and plan_type <> 1 and plan_type <> 2")
    int inProcessNum();

    @Select(value = "SELECT\n" +
            "\tplan.id as planId, plan.plan_type,item.file_key, item.file_type, file.path, file.url_path\n" +
            "FROM\n" +
            "\twater_temperature_plan plan\n" +
            "\tLEFT JOIN water_temperature_plan_file_items item ON plan.id = item.plan_id\n" +
            "\tLEFT JOIN file_items file ON item.file_key = file.file_key\n" +
            "WHERE\n" +
            "\tplan.id = ${planId} AND file.file_type = #{fileType}")
    WaterTempPlanFileItem selectWaterTempPlanFileItemByIdAndFileType(@Param("planId") Integer planId, @Param("fileType") String fileType);
}
