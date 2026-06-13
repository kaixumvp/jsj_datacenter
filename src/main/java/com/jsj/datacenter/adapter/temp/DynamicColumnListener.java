package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;

import java.util.List;
import java.util.Map;

public class DynamicColumnListener implements ReadListener<RiverChannelFlowTem> {

    private final List<String> head;

    public DynamicColumnListener(List<String> head) {
        this.head = head;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        if (rowIndex == 0) {
            head.add(String.valueOf(headMap.get(0).getNumberValue()));
            head.add(String.valueOf(headMap.get(1).getNumberValue()));
            head.add(String.valueOf(headMap.get(2).getNumberValue()));
        }
        ReadListener.super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(RiverChannelFlowTem riverChannelFlowTem, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
