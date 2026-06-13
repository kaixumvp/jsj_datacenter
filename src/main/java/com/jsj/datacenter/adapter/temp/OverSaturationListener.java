package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Lists;
import com.jsj.datacenter.infrastructure.vo.OverSaturationItemVO;
import lombok.Data;

import java.util.List;

@Data
public class OverSaturationListener implements ReadListener<OverSaturationItemVO> {

    private final List<OverSaturationItemVO> dataList = Lists.newArrayList();

    @Override
    public void invoke(OverSaturationItemVO data, AnalysisContext context) {
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
