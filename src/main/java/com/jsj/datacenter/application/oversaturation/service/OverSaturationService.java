package com.jsj.datacenter.application.oversaturation.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.jsj.datacenter.adapter.dto.oversaturation.OverSaturationDTO;
import com.jsj.datacenter.adapter.temp.OverSaturationListener;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturationPoint;
import com.jsj.datacenter.application.oversaturation.mapper.OverSaturationPointMapper;
import com.jsj.datacenter.infrastructure.common.utils.DateUtils;
import com.jsj.datacenter.infrastructure.common.utils.SecurityUtils;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import com.jsj.datacenter.infrastructure.vo.OverSaturationItemVO;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturation;
import com.jsj.datacenter.application.oversaturation.mapper.OverSaturationMapper;
import com.jsj.datacenter.infrastructure.vo.PointPositionItemVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OverSaturationService {

    @Autowired
    OverSaturationMapper overSaturationMapper;

    @Autowired
    FileItemMapper fileItemMapper;

    @Autowired
    OverSaturationPointMapper overSaturationPointMapper;

    public OverSaturationDTO parseOverSaturation(String fileKey) {
        FileItemPO file = fileItemMapper.getFile(fileKey);
        OverSaturationDTO overSaturationDTO = new OverSaturationDTO();

        try (ExcelReader excelReader = EasyExcel.read(file.getPath()).build()) {
            List<PointPositionItemVO> points = Lists.newArrayList();
            List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
            for (ReadSheet sheet : sheets) {
                PointPositionItemVO pointPositionItemVO = new PointPositionItemVO();
                OverSaturationListener overSaturationListener = new OverSaturationListener();
                sheet.setClazz(OverSaturationItemVO.class);
                sheet.setCustomReadListenerList(Lists.newArrayList(overSaturationListener));
                excelReader.read(sheet);
                pointPositionItemVO.setData(overSaturationListener.getDataList());
                pointPositionItemVO.setPointName(sheet.getSheetName());
                points.add(pointPositionItemVO);
            }
            overSaturationDTO.setFileKey(fileKey);
            overSaturationDTO.setPointPositions(points);
            overSaturationDTO.setPeriod(file.getFilename());
        }
        return overSaturationDTO;
    }

    public void addOverSaturation(OverSaturationDTO waterQualityAddReqDTO) {
        // 检查是否存在该周期的数据
        OverSaturation existingOverSaturation = overSaturationMapper.getByPeriod(waterQualityAddReqDTO.getPeriod());
        if (existingOverSaturation != null) {
            throw new IllegalArgumentException("该周期的过饱和数据已存在");
        }
        List<PointPositionItemVO> pointPositions = waterQualityAddReqDTO.getPointPositions();
        if (pointPositions == null || pointPositions.isEmpty()) {
            throw new IllegalArgumentException("该周期的过饱和数据为空");
        }
        OverSaturation waterQuality = new OverSaturation();
        waterQuality.setPeriod(waterQualityAddReqDTO.getPeriod());
        waterQuality.setFileKey(waterQualityAddReqDTO.getFileKey());
        //List<OverSaturationItemVO> items = waterQualityAddReqDTO.getData();
        /*if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("过饱和数据不能为空");
        }*/
        //String jsonString = JSONArray.toJSONString(items);
        //waterQuality.setData(jsonString);
        waterQuality.setCreateBy(SecurityUtils.getLoginUserId());
        waterQuality.setCreateTime(DateUtils.getTime());
        overSaturationMapper.insert(waterQuality);
        for (PointPositionItemVO pointPositionItemVO : pointPositions) {
            OverSaturationPoint overSaturationPoint = new OverSaturationPoint();
            overSaturationPoint.setData(JSONArray.toJSONString(pointPositionItemVO.getData()));
            overSaturationPoint.setPointName(pointPositionItemVO.getPointName());
            overSaturationPoint.setPeriod(waterQualityAddReqDTO.getPeriod());
            overSaturationPoint.setCreateBy(SecurityUtils.getLoginUserId());
            overSaturationPoint.setCreateTime(DateUtils.getTime());
            overSaturationPointMapper.insert(overSaturationPoint);
        }

    }

    public void editOverSaturation(OverSaturationDTO waterQualityAddReqDTO) {
        // 检查是否存在该周期的数据
        OverSaturation existingOverSaturation = overSaturationMapper.getByPeriod(waterQualityAddReqDTO.getPeriod());
        if (existingOverSaturation == null) {
            throw new IllegalArgumentException("该周期的过饱和数据不存在");
        }
        // 更新过饱和数据
        existingOverSaturation.setFileKey(waterQualityAddReqDTO.getFileKey());
        List<PointPositionItemVO> pointPositions = waterQualityAddReqDTO.getPointPositions();
        if (pointPositions == null || pointPositions.isEmpty()) {
            throw new IllegalArgumentException("过饱和数据不能为空");
        }
        overSaturationPointMapper.deleteByPeriod(waterQualityAddReqDTO.getPeriod());
        for (PointPositionItemVO pointPositionItemVO : pointPositions) {
            OverSaturationPoint overSaturationPoint = new OverSaturationPoint();
            overSaturationPoint.setData(JSONArray.toJSONString(pointPositionItemVO.getData()));
            overSaturationPoint.setPointName(pointPositionItemVO.getPointName());
            overSaturationPoint.setPeriod(waterQualityAddReqDTO.getPeriod());
            overSaturationPoint.setCreateBy(SecurityUtils.getLoginUserId());
            overSaturationPoint.setCreateTime(DateUtils.getTime());
            overSaturationPointMapper.insert(overSaturationPoint);
        }
    }

    public void deleteOverSaturation(String period) {
        // 检查是否存在该周期的数据
        OverSaturation existingOverSaturation = overSaturationMapper.getByPeriod(period);
        if (existingOverSaturation == null) {
            throw new IllegalArgumentException("该周期的过饱和数据不存在");
        }
        // 删除过饱和数据
        overSaturationMapper.removeByPeriod(period);
        overSaturationPointMapper.deleteByPeriod(period);
    }

    public List<OverSaturationDTO> getAllOverSaturation() {
        List<OverSaturation> overSaturations = overSaturationMapper.selectAllOverSaturationsWithOverSaturationPoints();
        overSaturations.stream().map(OverSaturation::getOverSaturationPoints).collect(Collectors.toList());
        return overSaturations.stream().sorted( new Comparator<OverSaturation>() {
                    @Override
                    public int compare(OverSaturation a, OverSaturation b) {
                        return b.getCreateTime().compareTo(a.getCreateTime());
                    }
                })
                .map(waterQuality -> {
                    OverSaturationDTO dto = new OverSaturationDTO();
                    dto.setPeriod(waterQuality.getPeriod());
                    dto.setFileKey(waterQuality.getFileKey());
                    List<OverSaturationPoint> overSaturationPoints = waterQuality.getOverSaturationPoints();
                    List<PointPositionItemVO> PointPositionItemVOs = Lists.newArrayList();
                    for (OverSaturationPoint overSaturationPoint : overSaturationPoints) {
                        PointPositionItemVO pointPositionItemVO = new PointPositionItemVO();
                        pointPositionItemVO.setPointName(overSaturationPoint.getPointName());
                        pointPositionItemVO.setData(JSONArray.parseArray(overSaturationPoint.getData(), OverSaturationItemVO.class));
                        PointPositionItemVOs.add(pointPositionItemVO);
                    }
                    dto.setPointPositions(PointPositionItemVOs);
                    return dto;
                }).collect(Collectors.toList());
    }

    public OverSaturationDTO getOverSaturationByPeriod(String period) {
        OverSaturation waterQuality = overSaturationMapper.getByPeriod(period);
        if (waterQuality == null) {
            throw new IllegalArgumentException("该周期的过饱和数据不存在");
        }
        OverSaturationDTO dto = new OverSaturationDTO();
        dto.setPeriod(waterQuality.getPeriod());
        dto.setFileKey(waterQuality.getFileKey());
        List<OverSaturationPoint> overSaturationPoints = overSaturationPointMapper.selectByPeriod(period);
        List<PointPositionItemVO> PointPositionItemVOs = Lists.newArrayList();
        for (OverSaturationPoint overSaturationPoint : overSaturationPoints) {
            PointPositionItemVO pointPositionItemVO = new PointPositionItemVO();
            pointPositionItemVO.setPointName(overSaturationPoint.getPointName());
            pointPositionItemVO.setData(JSONArray.parseArray(overSaturationPoint.getData(), OverSaturationItemVO.class));
            PointPositionItemVOs.add(pointPositionItemVO);
        }
        dto.setPointPositions(PointPositionItemVOs);
        return dto;
    }

    public List<String> getAllPeriods() {
        return overSaturationMapper.getByPeriods();
    }


}
