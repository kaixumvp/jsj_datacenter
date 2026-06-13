package com.jsj.datacenter.application.waschemadetail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.waschemadetail.domain.WaSchemaDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WaSchemaDetailMapper extends BaseMapper<WaSchemaDetail> {

    @Select("select * from wa_schema_detail where schema_id = #{schemaId}")
    List<WaSchemaDetail> selectBySchemaId(Integer schemaId);
}
