package com.jsj.datacenter.screen;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.temp.DischargeWaterTemperature;
import com.jsj.datacenter.adapter.temp.VerticalWaterTemperature;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReservoirPaintbrush {

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private Color xchatBackgroundColor;

    public List<VerticalWaterTemperature> analysisVerticalWaterTemperatureFile(String path, int sheet){
        return EasyExcel.read(path)
                .head(VerticalWaterTemperature.class)
                .sheet(sheet)
                .doReadSync();
    }

    public List<DischargeWaterTemperature> analysisDischargeWaterTemperature(String path, int sheet){
        return EasyExcel.read(path)
                .head(DischargeWaterTemperature.class)
                .sheet(sheet)
                .doReadSync();
    }

    // 画锤向水温
    public FileItemDTO paintbrushVerticalWT(List<VerticalWaterTemperature> verticalWaterTemperatureList) throws IOException {
        log.info("paintbrushVerticalWT:{}", JSON.toJSONString(verticalWaterTemperatureList));
        Map<LocalDate, List<VerticalWaterTemperature>> dateListMap = verticalWaterTemperatureList.stream().collect(Collectors.groupingBy(VerticalWaterTemperature::getDate));
        TreeMap<LocalDate, List<VerticalWaterTemperature>> localDateListTreeMap = new TreeMap<>(dateListMap);
        XYChart chart = new XYChartBuilder()
                .width(1320)
                .height(460)
                .title("垂向水温")
                .xAxisTitle("水温")
                .yAxisTitle("高程")
                .build();
        for(Map.Entry<LocalDate, List<VerticalWaterTemperature>> entry : localDateListTreeMap.entrySet()){
            List<VerticalWaterTemperature> dataList = entry.getValue();
            LocalDate localDate = entry.getKey();
            List<Double> waterLevels = dataList.stream().map(VerticalWaterTemperature::getWaterLevel).collect(Collectors.toList());
            List<Double> temperatures = dataList.stream().map(VerticalWaterTemperature::getTemperature).collect(Collectors.toList());
            XYSeries xySeries = chart.addSeries(localDate.getMonthValue() + "月", temperatures, waterLevels);
            xySeries.setLineWidth(2);
            xySeries.setMarker(SeriesMarkers.NONE);
        }
        return fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RIVER_OUT_CXWD,"cxsw.png", BitmapEncoder.getBufferedImage(chart));
    }

    // 画锤向水温对比图
    public FileItemDTO paintbrushHorizontalDiffWT(List<VerticalWaterTemperature> verticalWaterTemperatureList,
                                                    List<VerticalWaterTemperature> verticalWaterTemperatureDiffList) throws IOException {
        log.info("paintbrushHorizontalDiffWT: {},{}", JSON.toJSONString(verticalWaterTemperatureList), JSON.toJSONString(verticalWaterTemperatureDiffList));
        XYChart chart = new XYChartBuilder()
                .width(1320)
                .height(460)
                .title("垂向水温")
                .xAxisTitle("水温")
                .yAxisTitle("高程")
                .build();
        this.setChartStyle(chart);
        Map<LocalDate, List<VerticalWaterTemperature>> dateListMap = verticalWaterTemperatureList.stream().collect(Collectors.groupingBy(VerticalWaterTemperature::getDate));
        TreeMap<LocalDate, List<VerticalWaterTemperature>> localDateListTreeMap = new TreeMap<>(dateListMap);
        for(Map.Entry<LocalDate, List<VerticalWaterTemperature>> entry : localDateListTreeMap.entrySet()){
            List<VerticalWaterTemperature> dataList = entry.getValue();
            LocalDate localDate = entry.getKey();
            List<Double> waterLevels = dataList.stream().map(VerticalWaterTemperature::getWaterLevel).collect(Collectors.toList());
            List<Double> temperatures = dataList.stream().map(VerticalWaterTemperature::getTemperature).collect(Collectors.toList());
            XYSeries xySeries = chart.addSeries(localDate.getMonthValue() + "月", temperatures, waterLevels);
            xySeries.setLineWidth(2);
            xySeries.setMarker(SeriesMarkers.NONE);
        }

        if (!CollectionUtils.isEmpty(verticalWaterTemperatureDiffList)) {
            Map<LocalDate, List<VerticalWaterTemperature>> dateListDiffMap = verticalWaterTemperatureDiffList.stream().collect(Collectors.groupingBy(VerticalWaterTemperature::getDate));
            TreeMap<LocalDate, List<VerticalWaterTemperature>> localDateListDiffTreeMap = new TreeMap<>(dateListDiffMap);
            for(Map.Entry<LocalDate, List<VerticalWaterTemperature>> entry : localDateListDiffTreeMap.entrySet()){
                List<VerticalWaterTemperature> dataList = entry.getValue();
                LocalDate localDate = entry.getKey();
                List<Double> waterLevels = dataList.stream().map(VerticalWaterTemperature::getWaterLevel).collect(Collectors.toList());
                List<Double> temperatures = dataList.stream().map(VerticalWaterTemperature::getTemperature).collect(Collectors.toList());
                XYSeries xySeries = chart.addSeries("实测"+localDate.getMonthValue() + "月", temperatures, waterLevels);
                xySeries.setLineWidth(3);
                xySeries.setMarker(SeriesMarkers.NONE);
            }
        }
        return fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RIVER_OUT_CXWD_DIFF,"cxsw_diff.png", BitmapEncoder.getBufferedImage(chart));
    }

    public FileItemDTO paintbrushXXWT(List<DischargeWaterTemperature> dischargeWaterTemperatures, List<DischargeWaterTemperature> dischargeWaterTemperaturesDiff) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(1320)
                .height(460)
                .title("下泄水温")
                .xAxisTitle("天数")
                .yAxisTitle("温度")
                .build();
        this.setChartStyle(chart);
        List<Integer> waterLevels = dischargeWaterTemperatures.stream().map(DischargeWaterTemperature::getMonth).collect(Collectors.toList());
        List<Double> temperatures = dischargeWaterTemperatures.stream().map(DischargeWaterTemperature::getAverageTemperature).collect(Collectors.toList());
        XYSeries xySeries = chart.addSeries("下泄水温", waterLevels, temperatures);
        xySeries.setLineWidth(2);
        xySeries.setMarker(SeriesMarkers.NONE);
        if (!CollectionUtils.isEmpty(dischargeWaterTemperaturesDiff)) {
            List<Integer> waterLevelsDiff = dischargeWaterTemperaturesDiff.stream().map(DischargeWaterTemperature::getMonth).collect(Collectors.toList());
            List<Double> temperaturesDiff = dischargeWaterTemperaturesDiff.stream().map(DischargeWaterTemperature::getAverageTemperature).collect(Collectors.toList());
            XYSeries xySeriesDiff = chart.addSeries("对比下泄水温", waterLevelsDiff, temperaturesDiff);
            xySeriesDiff.setLineWidth(4);
            xySeriesDiff.setMarker(SeriesMarkers.NONE);
        }
        return fileItemService.saveImageFile(UploadFileType.WA_SCHEMA_PARAM_RIVER_OUT_XX_DIFF,"xxsw_diff.png", BitmapEncoder.getBufferedImage(chart));

    }
    private void setChartStyle(XYChart chart) {
        chart.getStyler().setChartBackgroundColor(xchatBackgroundColor);
        chart.getStyler().setLegendBackgroundColor(xchatBackgroundColor);
        chart.getStyler().setLegendFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        chart.getStyler().setChartFontColor(Color.WHITE);
        chart.getStyler().setAxisTickLabelsColor(Color.WHITE);
        chart.getStyler().setAxisTickLabelsFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        chart.getStyler().setChartTitleFont(new Font("Microsoft YaHei", Font.BOLD, 27));
        chart.getStyler().setAxisTitleFont(new Font("Microsoft YaHei", Font.BOLD, 22));
    }
}
