package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

@Data
public class LayeredWaterData {
    private Integer month;
    private Double naturalWaterTemp;
    private Double ydWaterTemp;
    private Double wdWaterTemp;
    private Double difference;
}
