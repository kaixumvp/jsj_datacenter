package com.jsj.datacenter.application.waschemadetail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsj.datacenter.application.waschemadetail.domain.WaSchemaDetail;
import com.jsj.datacenter.application.waschemadetail.mapper.WaSchemaDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WaSchemaDetailService extends ServiceImpl<WaSchemaDetailMapper, WaSchemaDetail> {

    @Autowired
    WaSchemaDetailMapper waSchemaDetailMapper;


    public List<WaSchemaDetail> getBySchemaId(Integer schemaId) {
        return waSchemaDetailMapper.selectBySchemaId(schemaId);
    }
}
