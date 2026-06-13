package com.jsj.datacenter;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.jsj.datacenter.adapter.dto.oversaturation.OverSaturationDTO;
import com.jsj.datacenter.adapter.temp.OverSaturationListener;
import com.jsj.datacenter.infrastructure.vo.OverSaturationItemVO;
import com.jsj.datacenter.infrastructure.vo.PointPositionItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OverSaturationTest {

    @Test
    public void resultJson(){
        List<OverSaturationDTO> overSaturationList = Lists.newArrayList();
        OverSaturationDTO overSaturationDTO = new OverSaturationDTO();
        overSaturationList.add(overSaturationDTO);
        for(OverSaturationDTO overSaturationDTO1:overSaturationList){

            PointPositionItemVO pointPositionItemVO = new PointPositionItemVO();
            List<PointPositionItemVO> points = Lists.newArrayList();
            pointPositionItemVO.setPointName("第一个点位");
            String dataStr = "[{\"name\": \"总溶解气体饱和度\", \"unit\": \"%\", \"value\": \"60\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"溶解氧\", \"unit\": \"NTU\", \"value\": \"20\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"总溶解气体\", \"unit\": \"mg/L\", \"value\": \"30\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"水温\", \"unit\": \"℃\", \"value\": \"15\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"气压\", \"unit\": \"hpa\", \"value\": \"20\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}]";
            pointPositionItemVO.setData(JSONArray.parseArray(dataStr, OverSaturationItemVO.class));
            points.add(pointPositionItemVO);

            PointPositionItemVO pointPositionItemVO2 = new PointPositionItemVO();
            pointPositionItemVO2.setPointName("第二个点位");
            String dataStr2 = "[{\"name\": \"总溶解气体饱和度\", \"unit\": \"%\", \"value\": \"60\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"溶解氧\", \"unit\": \"NTU\", \"value\": \"20\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"总溶解气体\", \"unit\": \"mg/L\", \"value\": \"30\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"水温\", \"unit\": \"℃\", \"value\": \"15\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}, {\"name\": \"气压\", \"unit\": \"hpa\", \"value\": \"20\", \"latitude\": \"20.454565\", \"longitude\": \"101.2222\"}]";
            pointPositionItemVO2.setData(JSONArray.parseArray(dataStr2, OverSaturationItemVO.class));
            points.add(pointPositionItemVO2);

            overSaturationDTO1.setPeriod("第一期");
            overSaturationDTO1.setFileKey("");
            overSaturationDTO1.setPointPositions(points);
        }
        System.out.println(JSONObject.toJSONString(ResponseEntity.ok(overSaturationList)));
    }

    @Test
    public void excel() {
        try (ExcelReader excelReader = EasyExcel.read("D:\\ideaProject\\uploads\\over_saturation_data\\aaaa.xlsx").build()) {
            OverSaturationDTO overSaturationDTO = new OverSaturationDTO();
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
            overSaturationDTO.setFileKey("");
            overSaturationDTO.setPointPositions(points);
            overSaturationDTO.setPeriod("第一期");
            System.out.println(JSONObject.toJSONString(overSaturationDTO));
        }
    }
}
