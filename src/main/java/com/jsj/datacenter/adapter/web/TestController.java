package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("test")
@EasyResponse
public class TestController {

    @RequestMapping("data")
    public JSONArray getData(){
        List<DataTest> dataTests = Lists.newArrayList();
        for (int i = 1; i <= 24; i++) {
            DataTest dataTest = new DataTest();
            dataTest.setName(i+"月");
            dataTest.setStack("Total");
            dataTest.setType("line");
            List<Double> datas = Lists.newArrayList();
            for (int j = 1; j <= 175; j++) {
                BigDecimal two = new BigDecimal(Math.random()*50+1);
                datas.add(two.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            dataTest.setData(datas);
            dataTests.add(dataTest);
        }
        return JSONArray.of(dataTests);
    }
}

@Data
class DataTest{
    /*name: 'Email',
    type: 'line',
    stack: 'Total',
    data: [120, 132, 101, 134, 90, 230, 210]*/
    private String type;
    private String name;
    private String stack;
    private List<Double> data;
}
