package com.jsj.datacenter.application.waschemaresult.service;

import com.jsj.datacenter.application.waschemaresult.mapper.WaSchemaResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WaSchemaResultService {

    @Autowired
    WaSchemaResultMapper waSchemaResultMapper;


}
