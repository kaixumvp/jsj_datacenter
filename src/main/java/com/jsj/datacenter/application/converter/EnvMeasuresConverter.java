package com.jsj.datacenter.application.converter;

import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresDTO;
import com.jsj.datacenter.application.envmeasures.domain.EnvMeasures;
import org.mapstruct.Mapper;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Mapper(componentModel = "spring")
public interface EnvMeasuresConverter {
    EnvMeasuresDTO poToDto(EnvMeasures po);
    EnvMeasures dtoToPo(EnvMeasuresDTO dto);
}
