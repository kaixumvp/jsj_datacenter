package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

import java.util.List;

@Data
public class WaterTemperatureReportVO {

    private String reportName;

    private List<Integer> relatedTaskIds;
}
