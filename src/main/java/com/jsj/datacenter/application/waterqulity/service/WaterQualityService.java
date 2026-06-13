package com.jsj.datacenter.application.waterqulity.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.jsj.datacenter.adapter.dto.waterquality.WaterQualityDTO;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturation;
import com.jsj.datacenter.infrastructure.common.utils.DateUtils;
import com.jsj.datacenter.infrastructure.common.utils.SecurityUtils;
import com.jsj.datacenter.infrastructure.vo.WaterQualityItemVO;
import com.jsj.datacenter.application.waterqulity.domain.WaterQuality;
import com.jsj.datacenter.application.waterqulity.mapper.WaterQualityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WaterQualityService {

    @Autowired
    WaterQualityMapper waterQualityMapper;

    public void addWaterQuality(WaterQualityDTO waterQualityAddReqDTO) {
        // 检查是否存在该周期的数据
        WaterQuality existingWaterQuality = waterQualityMapper.getByPeriod(waterQualityAddReqDTO.getPeriod());
        if (existingWaterQuality != null) {
            throw new IllegalArgumentException("该周期的水质数据已存在");
        }
        WaterQuality waterQuality = new WaterQuality();
        waterQuality.setPeriod(waterQualityAddReqDTO.getPeriod());
        waterQuality.setFileKey(waterQualityAddReqDTO.getFileKey());
        List<WaterQualityItemVO> items = waterQualityAddReqDTO.getData();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("水质数据不能为空");
        }
        String jsonString = JSONArray.toJSONString(items);
        waterQuality.setData(jsonString);
        waterQuality.setCreateBy(SecurityUtils.getLoginUserId());
        waterQuality.setCreateTime(DateUtils.getTime());
        waterQualityMapper.insert(waterQuality);
    }

    public void editWaterQuality(WaterQualityDTO waterQualityAddReqDTO) {
        // 检查是否存在该周期的数据
        WaterQuality existingWaterQuality = waterQualityMapper.getByPeriod(waterQualityAddReqDTO.getPeriod());
        if (existingWaterQuality == null) {
            throw new IllegalArgumentException("该周期的水质数据不存在");
        }
        // 更新水质数据
        existingWaterQuality.setFileKey(waterQualityAddReqDTO.getFileKey());
        List<WaterQualityItemVO> items = waterQualityAddReqDTO.getData();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("水质数据不能为空");
        }
        existingWaterQuality.setData(JSONArray.toJSONString(items));
        waterQualityMapper.updateByPeriod(existingWaterQuality);

    }

    public void deleteWaterQuality(String period) {
        // 检查是否存在该周期的数据
        WaterQuality existingWaterQuality = waterQualityMapper.getByPeriod(period);
        if (existingWaterQuality == null) {
            throw new IllegalArgumentException("该周期的水质数据不存在");
        }
        // 删除水质数据
        waterQualityMapper.removeByPeriod(period);

    }

    public List<WaterQualityDTO> getAllWaterQuality() {
        List<WaterQuality> waterQualities = waterQualityMapper.selectList(null);

        return waterQualities.stream().sorted( new Comparator<WaterQuality>() {
                    @Override
                    public int compare(WaterQuality a, WaterQuality b) {
                        return b.getCreateTime().compareTo(a.getCreateTime());
                    }
                })
                .map(waterQuality -> {
                    WaterQualityDTO dto = new WaterQualityDTO();
                    dto.setPeriod(waterQuality.getPeriod());
                    dto.setFileKey(waterQuality.getFileKey());
                    List<WaterQualityItemVO> items = JSON.parseArray(waterQuality.getData(), WaterQualityItemVO.class);
                    dto.setData(items);
                    return dto;
                }).collect(Collectors.toList());
    }

    public WaterQualityDTO getWaterQualityByPeriod(String period) {
        WaterQuality waterQuality = waterQualityMapper.getByPeriod(period);
        if (waterQuality == null) {
            throw new IllegalArgumentException("该周期的水质数据不存在");
        }
        WaterQualityDTO dto = new WaterQualityDTO();
        dto.setPeriod(waterQuality.getPeriod());
        dto.setFileKey(waterQuality.getFileKey());
        List<WaterQualityItemVO> items = JSON.parseArray(waterQuality.getData(), WaterQualityItemVO.class);
        dto.setData(items);
        return dto;
    }

    public List<String> getAllPeriods() {
        return waterQualityMapper.getByPeriods();
    }
}
