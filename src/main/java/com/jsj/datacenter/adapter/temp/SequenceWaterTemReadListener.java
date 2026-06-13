package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SequenceWaterTemReadListener implements ReadListener<SequenceWaterTem> {

    private final List<List<String>> head;

    public SequenceWaterTemReadListener(List<List<String>> head) {
        this.head = head;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        if (rowIndex == 3) {
            List<String> headItem1 = Lists.newArrayList();
            headItem1.add(headMap.get(1).getStringValue());
            List<String> headItem2 = Lists.newArrayList();
            headItem2.add(headMap.get(2).getStringValue());
            List<String> headItem3 = Lists.newArrayList();
            headItem3.add(headMap.get(3).getStringValue());
            head.add(headItem1);
            head.add(headItem2);
            head.add(headItem3);
        }
    }

    @Override
    public void invoke(SequenceWaterTem sequenceWaterTem, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
