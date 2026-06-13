package com.jsj.datacenter.application.watertemperature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperatureReports;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaterTemperatureReportsMapper extends BaseMapper<WaterTemperatureReports> {

    @Select(value = "select * from water_temperature_reports where report_name LIKE '%${reportName}%' limit #{startIndex}, #{endIndex}")
    List<WaterTemperatureReports> selectListLikeNameLimitPage(@Param("reportName") String reportName,
                                                           @Param("startIndex") Integer startIndex,
                                                           @Param("endIndex") Integer endIndex);

    @Select(value = "select count(*) from water_temperature_reports where report_name LIKE '%${reportName}%' ")
    int selectListLikeNameCount(@Param("reportName") String reportName);

    @Select(value = "select count(*) from water_temperature_reports where report_status LIKE '%${reportStatus}%' ")
    int selectCountByReportStatus(@Param("reportStatus") Integer reportStatus);



}
