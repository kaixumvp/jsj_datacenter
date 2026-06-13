package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;

import java.util.List;

public class RiverChannelMeteorologyListener implements ReadListener<RiverChannelMeteorologyTem> {

    private int currentRows = 0;
    private final List<RiverChannelMainWaterTemp> waterTemps;
    public RiverChannelMeteorologyListener(List<RiverChannelMainWaterTemp> waterTemps) {
        this.waterTemps = waterTemps;
    }

    @Override
    public void invoke(RiverChannelMeteorologyTem riverChannelMeteorologyTem, AnalysisContext analysisContext) {
        riverChannelMeteorologyTem.setWaterTemp(waterTemps.get(currentRows).getWaterTemp());
        currentRows++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
