package com.jsj.datacenter.application.waschemaresult.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsj.datacenter.application.waschemadetail.domain.WaSchemaDetail;
import com.jsj.datacenter.application.waschemaresult.domain.WaSchemaCalculateResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WaSchemaResultMapper extends BaseMapper<WaSchemaCalculateResult> {

}
