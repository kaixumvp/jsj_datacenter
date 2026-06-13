package com.jsj.datacenter.infrastructure.vo;

import lombok.Data;

import java.util.List;

@Data
public class PointPositionItemVO {
    private String pointName;
    private List<OverSaturationItemVO> data;
}
