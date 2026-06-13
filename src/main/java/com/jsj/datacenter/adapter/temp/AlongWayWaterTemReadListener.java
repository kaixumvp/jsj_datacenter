package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlongWayWaterTemReadListener implements ReadListener<Map<Integer, String>> {

    Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMap;
    int day = 1;

    public AlongWayWaterTemReadListener(Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMap) {
        this.alongWayWaterTemMap = alongWayWaterTemMap;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        if (StringUtils.contains(data.get(0), "Zone")){
            day++;
        } else {
            List<AlongWayWaterTem> alongWayWaterTems = alongWayWaterTemMap.get(day);
            if (alongWayWaterTems == null){
                alongWayWaterTems = new ArrayList<>();
                alongWayWaterTemMap.put(day, alongWayWaterTems);
            }
            AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
            alongWayWaterTem.setDistance(Double.valueOf(data.get(1)));
            alongWayWaterTem.setWaterTem(Double.valueOf(data.get(2)));
            alongWayWaterTem.setAirTem(Double.valueOf(data.get(3)));
            alongWayWaterTems.add(alongWayWaterTem);
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
