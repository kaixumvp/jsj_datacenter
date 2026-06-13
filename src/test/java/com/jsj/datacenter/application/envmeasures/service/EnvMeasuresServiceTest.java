package com.jsj.datacenter.application.envmeasures.service;

import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresDTO;
import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresListDTO;
import com.jsj.datacenter.application.converter.EnvMeasuresConverter;
import com.jsj.datacenter.application.envmeasures.domain.EnvMeasures;
import com.jsj.datacenter.application.envmeasures.mapper.EnvMeasuresMapper;
import com.jsj.datacenter.infrastructure.common.enums.EvnMeasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvMeasuresServiceTest {

    @Mock
    private EnvMeasuresMapper envMeasuresMapper;

    @Mock
    private EnvMeasuresConverter converter;

    @InjectMocks
    private EnvMeasuresService envMeasuresService;

    private EnvMeasures testPo;
    private EnvMeasuresDTO testDto;

    @BeforeEach
    void setUp() {
        testPo = new EnvMeasures();
        testPo.setId(1L);
        testPo.setEventType(EvnMeasureType.ENV_APPROVAL);
        testPo.setTitle("环评批复");
        testPo.setWeigh(10);
        testPo.setContent("批复内容");
        testPo.setImplCondition(1);
        testPo.setProcess("流程A");
        testPo.setCreateTime("2025-06-01");
        testPo.setUpdateTime("2025-06-02");
        testPo.setCreateBy("admin");
        testPo.setUpdateBy("admin");

        testDto = new EnvMeasuresDTO();
        testDto.setId(1L);
        testDto.setTitle("环评批复");
        testDto.setWeigh(10);
        testDto.setContent("批复内容");
        testDto.setImplCondition(1);
        testDto.setProcess("流程A");
    }

    @Test
    void getEnvMeasures_returnsConvertedDto() {
        when(converter.poToDto(testPo)).thenReturn(testDto);

        EnvMeasuresDTO result = envMeasuresService.getEnvMeasures(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("环评批复", result.getTitle());
        verify(envMeasuresMapper).selectById(1L);
        verify(converter).poToDto(testPo);
    }

    @Test
    void getAllEnvMeasures_withEmptyList_returnsInitializedResult() {
        when(envMeasuresMapper.selectList(null)).thenReturn(Collections.emptyList());

        EnvMeasuresListDTO result = envMeasuresService.getAllEnvMeasures();

        assertNotNull(result);
        verify(envMeasuresMapper).selectList(null);
    }

    @Test
    void getAllEnvMeasures_withData_groupsByTitle() {
        EnvMeasures po2 = new EnvMeasures();
        po2.setId(2L);
        po2.setEventType(EvnMeasureType.ENV_EFFECT);
        po2.setTitle("环评批复");
        po2.setWeigh(5);
        po2.setContent("内容B");

        EnvMeasuresDTO dto2 = new EnvMeasuresDTO();
        dto2.setId(2L);
        dto2.setTitle("环评批复");
        dto2.setWeigh(5);
        dto2.setContent("内容B");

        when(envMeasuresMapper.selectList(null)).thenReturn(Arrays.asList(testPo, po2));
        when(converter.poToDto(testPo)).thenReturn(testDto);
        when(converter.poToDto(po2)).thenReturn(dto2);

        EnvMeasuresListDTO result = envMeasuresService.getAllEnvMeasures();

        assertNotNull(result);
        verify(converter, times(2)).poToDto(any(EnvMeasures.class));
    }

    @Test
    void createEnvMeasures_callsConverterAndInsert() {
        when(converter.dtoToPo(testDto)).thenReturn(testPo);

        envMeasuresService.createEnvMeasures(testDto);

        verify(converter).dtoToPo(testDto);
        verify(envMeasuresMapper).insert(testPo);
    }

    @Test
    void updateEnvMeasures_callsConverterAndUpdateById() {
        when(converter.dtoToPo(testDto)).thenReturn(testPo);

        envMeasuresService.updateEnvMeasures(testDto);

        verify(converter).dtoToPo(testDto);
        verify(envMeasuresMapper).updateById(testPo);
    }

    @Test
    void deleteEnvMeasures_callsDeleteById() {
        envMeasuresService.deleteEnvMeasures(1L);

        verify(envMeasuresMapper).deleteById(1L);
    }
}
