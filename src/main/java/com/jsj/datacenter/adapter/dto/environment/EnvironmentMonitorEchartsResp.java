package com.jsj.datacenter.adapter.dto.environment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 环境监测ECharts数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("环境监测ECharts数据响应")
public class EnvironmentMonitorEchartsResp {

    @ApiModelProperty("温度监测数据")
    private TemperatureData temperatureData;

    @ApiModelProperty("颗粒物监测数据")
    private ParticulateMatterData particulateMatterData;

    @ApiModelProperty("TSP监测数据")
    private TspData tspData;

    @ApiModelProperty("风速监测数据")
    private WindSpeedData windSpeedData;

    @ApiModelProperty("噪音分布监测数据")
    private NoiseDistributionData noiseDistributionData;

    @ApiModelProperty("风险监测数据")
    private RiskData riskData;

    /**
     * 温度监测数据（包含温度和湿度）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemperatureData {
        @ApiModelProperty("时间轴")
        private List<String> timeAxis;

        @ApiModelProperty("温度数据")
        private List<Float> temperature;

        @ApiModelProperty("湿度数据")
        private List<Float> humidity;
    }

    /**
     * 颗粒物监测数据（PM2.5和PM10）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticulateMatterData {
        @ApiModelProperty("时间轴")
        private List<String> timeAxis;

        @ApiModelProperty("PM2.5数据")
        private List<Float> pm25;

        @ApiModelProperty("PM10数据")
        private List<Float> pm10;
    }

    /**
     * TSP监测数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TspData {
        @ApiModelProperty("时间轴")
        private List<String> timeAxis;

        @ApiModelProperty("TSP数据")
        private List<Float> tsp;
    }

    /**
     * 风速监测数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WindSpeedData {
        @ApiModelProperty("时间轴")
        private List<String> timeAxis;

        @ApiModelProperty("风速数据")
        private List<Float> windSpeed;

        @ApiModelProperty("风力数据")
        private List<Float> windPower;
    }

    /**
     * 噪音分布监测数据（按区间统计）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoiseDistributionData {
        @ApiModelProperty("噪音区间标签")
        private List<String> ranges;

        @ApiModelProperty("各区间占比百分比")
        private List<Float> percentages;

        @ApiModelProperty("各区间数量")
        private List<Integer> counts;
    }

    /**
     * 风险监测数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskData {
        @ApiModelProperty("设备列表")
        private List<String> devices;

        @ApiModelProperty("风险等级数据")
        private List<RiskLevelData> riskLevels;
    }

    /**
     * 风险等级数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskLevelData {
        @ApiModelProperty("风险等级名称")
        private String levelName;

        @ApiModelProperty("风险值")
        private Float value;

        @ApiModelProperty("颜色标识")
        private String color;
    }
}