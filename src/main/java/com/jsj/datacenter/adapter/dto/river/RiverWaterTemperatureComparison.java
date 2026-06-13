package com.jsj.datacenter.adapter.dto.river;

import lombok.Data;

@Data
public class RiverWaterTemperatureComparison {

    private Integer month;

    private Double naturalDamSiteTemp;
    private Double naturalS35Temp;

    private Double riverDamSiteTemp;
    private Double riverS35Temp;

    private Double riverDamSiteTempDiff;
    private Double riverS35TempDiff;

    private Double alongNaturalTempDiff;
    private Double alongRiverTempDiff;
    private Double tempDiff;
}
