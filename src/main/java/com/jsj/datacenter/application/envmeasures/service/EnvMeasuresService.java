package com.jsj.datacenter.application.envmeasures.service;

import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresDTO;
import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresListDTO;
import com.jsj.datacenter.application.converter.EnvMeasuresConverter;
import com.jsj.datacenter.application.envmeasures.mapper.EnvMeasuresMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Service
@Slf4j
public class EnvMeasuresService {
    @Autowired
    private EnvMeasuresMapper envMeasuresMapper;
    @Autowired
    private EnvMeasuresConverter converter;

    public EnvMeasuresDTO getEnvMeasures(Long id) {
        return converter.poToDto(envMeasuresMapper.selectById(id));
    }

    public EnvMeasuresListDTO getAllEnvMeasures() {
        List<EnvMeasuresDTO> measuresDTOS = envMeasuresMapper.selectList(null).stream()
                .map(converter::poToDto)
                .collect(Collectors.toList());
        EnvMeasuresListDTO result = new EnvMeasuresListDTO();
        result.init();
        Map<String, EnvMeasuresListDTO.Measure> measures = new HashMap<>();
        measuresDTOS.forEach(item -> {
            EnvMeasuresListDTO.Measure measure = measures.getOrDefault(item.getTitle(), new EnvMeasuresListDTO.Measure());
            measure.addItem(item);
            measures.put(item.getTitle(), measure);
        });
        measures.forEach((k, v) -> result.addMeasure(v));
        return result;
    }

    public void createEnvMeasures(EnvMeasuresDTO dto) {
        envMeasuresMapper.insert(converter.dtoToPo(dto));
    }

    public void updateEnvMeasures(EnvMeasuresDTO dto) {
        envMeasuresMapper.updateById(converter.dtoToPo(dto));
    }

    public void deleteEnvMeasures(Long id) {
        envMeasuresMapper.deleteById(id);
    }
}
