package com.jsj.datacenter;

import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.temp.SequenceWaterTem;
import com.jsj.datacenter.adapter.temp.VerticalWaterTemperature;
import com.jsj.datacenter.application.oversaturation.domain.OverSaturation;
import com.jsj.datacenter.application.oversaturation.mapper.OverSaturationMapper;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.mapper.WaterTemperaturePlanMapper;
import com.jsj.datacenter.application.watertemperature.service.ReservoirService;
import com.jsj.datacenter.application.watertemperature.service.RiverService;
import com.jsj.datacenter.application.watertemperature.service.WaterTemperaturePlanService;
import com.jsj.datacenter.screen.ReservoirPaintbrush;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class DatacenterApplicationTests {

    @Autowired
    RiverService riverService;

    @Autowired
    private WaterTemperaturePlanMapper waterTemperaturePlanMapper;

    @Autowired
    private ReservoirService reservoirService;

    @Autowired
    private WaterTemperaturePlanService waterTemperaturePlanService;

    @Autowired
    private ReservoirPaintbrush reservoirPaintbrush;

    @Autowired
    private OverSaturationMapper overSaturationMapper;

    @Test
    void contextLoads() {
    }



    @Test
    void testInsertWaterTemperaturePlan() {
        WaterTemperaturePlan waterTemperaturePlan = new WaterTemperaturePlan();
        waterTemperaturePlan.setPlanName("方案名称");
        waterTemperaturePlanMapper.insert(waterTemperaturePlan);
        System.out.println(JSONObject.toJSONString(waterTemperaturePlan));
    }

    @Test
    void overSaturationMapperT() {
        List<OverSaturation> overSaturations = overSaturationMapper.selectAllOverSaturationsWithOverSaturationPoints();
        System.out.println(JSONObject.toJSONString(overSaturations));
    }

    @Test
    public void image() throws IOException {
        WaterTemperaturePlan waterTemperaturePlan = new WaterTemperaturePlan();
        waterTemperaturePlan.setId(20);
        List<SequenceWaterTem> sequenceWaterTems = Lists.newArrayList();
        //reservoirService.handleData(waterTemperaturePlan, sequenceWaterTems);
        //reservoirService.writeProfileWithCharBuffer();
    }

    @Test
    public void getData(){
        RiverCalculateResult riverCalculateResult = waterTemperaturePlanService.obtainCalculate(20);
        System.out.println(JSONObject.toJSONString(riverCalculateResult));
    }

    @Test
    public void changeReadFile() throws IOException {
        WaterTemperaturePlan waterTemperaturePlan =  new WaterTemperaturePlan();
        waterTemperaturePlan.setSolarcoe(0.23);
        waterTemperaturePlan.setMecovertcoe(0.2);
        waterTemperaturePlan.setWindcoe(0.5);
        reservoirService.changeReadFile(waterTemperaturePlan);
    }

    @Test
    public void executeXXScript() throws IOException, InterruptedException {
        //riverService.executeXXScript(0);
    }

    @Test
    public void testVerticalTemp() throws IOException {
        List<VerticalWaterTemperature> verticalWaterTemperatures
                = reservoirPaintbrush.analysisVerticalWaterTemperatureFile("D:/idea_pro/jinshajiang/script/template/垂向水温结构.xlsx",0);
        FileItemDTO fileItemDTO = reservoirPaintbrush.paintbrushVerticalWT(verticalWaterTemperatures);
        System.out.println(JSONObject.toJSONString(fileItemDTO));
    }


}
