package com.jsj.datacenter;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsj.datacenter.adapter.temp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiffCommExcelTest {
    List<VerticalWaterTemperature> verticalWaterTemperatureList;
    List<VerticalWaterTemperature> verticalWaterComTemperatureList;

    List<DischargeWaterTemperature> dischargeWaterTemperatureList;
    List<DischargeWaterTemperature> dischargeWaterComTemperatureList;

    @BeforeEach
    public void test() {
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/input/水库模型对比数据模板.xlsx";
        verticalWaterTemperatureList = EasyExcel.read(file_dir_input)
                .sheet(0)
                .head(VerticalWaterTemperature.class)
                .doReadSync();

        String file_dir_input_com = rootDir + "/input/水库模型对比数据模板(1).xlsx";
        verticalWaterComTemperatureList = EasyExcel.read(file_dir_input_com)
                .sheet(0)
                .head(VerticalWaterTemperature.class)
                .doReadSync();

        String file_dir_dis_input = rootDir + "/input/水库模型对比数据模板(1).xlsx";
        dischargeWaterTemperatureList = EasyExcel.read(file_dir_dis_input)
                .sheet(1)
                .head(DischargeWaterTemperature.class)
                .doReadSync();

        String file_dir_dis_input_com = rootDir + "/input/水库模型对比数据模板(1).xlsx";
        dischargeWaterComTemperatureList = EasyExcel.read(file_dir_dis_input_com)
                .sheet(1)
                .head(DischargeWaterTemperature.class)
                .doReadSync();

    }

    //垂向水温
    @Test
    public void verticalWaterTemperatureTest(){
        List<VerticalWaterResidualTemp> verticalWaterResidualTemps = new ArrayList<>();
        for (VerticalWaterTemperature verticalWaterTemperature : verticalWaterComTemperatureList) {
            VerticalWaterTemperature verticalWaterTemperatureCom = verticalWaterComTemperatureList.stream()
                    .filter(item -> item.equals(verticalWaterTemperature))
                    .findFirst().orElse(null);
            if (verticalWaterTemperatureCom != null) {
                VerticalWaterResidualTemp verticalWaterResidualTemp = new VerticalWaterResidualTemp();
                BeanUtils.copyProperties(verticalWaterTemperature, verticalWaterResidualTemp);
                verticalWaterResidualTemp.setComparativeTemperature(verticalWaterTemperatureCom.getTemperature());
                verticalWaterResidualTemp.setResidual(verticalWaterTemperature.getTemperature()- verticalWaterTemperatureCom.getTemperature());
                verticalWaterResidualTemps.add(verticalWaterResidualTemp);
            }
        }
        System.out.println(JSONObject.toJSONString(verticalWaterResidualTemps));

        //方差逻辑
        List<Double> residualSqCollect = verticalWaterResidualTemps.stream()
                .map(item -> Math.pow(item.getResidual(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        System.out.println("------------------"+sum);
    }

    //下泄
    @Test
    public void dischargeWaterTemperatureTest(){
        List<DischargeWaterResidualTemperature> dischargeWaterResidualTemperatures = new ArrayList<>();
        for (DischargeWaterTemperature dischargeWaterTemperature : dischargeWaterTemperatureList) {
            DischargeWaterTemperature dischargeWaterTemperatureCom = dischargeWaterComTemperatureList.stream()
                    .filter(item -> Objects.equals(item.getMonth(), dischargeWaterTemperature.getMonth()))
                    .findFirst().orElse(null);
            if (dischargeWaterTemperatureCom != null) {
                DischargeWaterResidualTemperature dischargeWaterResidualTemperature = new DischargeWaterResidualTemperature();
                BeanUtils.copyProperties(dischargeWaterTemperature, dischargeWaterResidualTemperature);
                dischargeWaterResidualTemperature.setComparativeTemperature(dischargeWaterTemperatureCom.getAverageTemperature());
                if (dischargeWaterTemperature.getAverageTemperature() == null){
                    dischargeWaterTemperature.setAverageTemperature(0.0);
                }
                if (dischargeWaterTemperatureCom.getAverageTemperature() == null){
                    dischargeWaterTemperatureCom.setAverageTemperature(0.0);
                }
                dischargeWaterResidualTemperature.setResidual(dischargeWaterTemperature.getAverageTemperature()-dischargeWaterTemperatureCom.getAverageTemperature());
                dischargeWaterResidualTemperatures.add(dischargeWaterResidualTemperature);
            }
        }
        System.out.println(JSONObject.toJSONString(dischargeWaterResidualTemperatures));

        //方差逻辑
        List<Double> residualSqCollect = dischargeWaterResidualTemperatures.stream()
                .map(item -> Math.pow(item.getResidual(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);
    }

    @Test
    public void hdAlongWayWaterTest(){
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/input/河道模型对比数据模板.xlsx";
        Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMap = Maps.newHashMap();
        EasyExcel.read(file_dir_input)
                .registerReadListener(new AlongWayWaterTemReadListener(alongWayWaterTemMap))
                .headRowNumber(4)
                //.head(AlongWayWaterTem.class)
                .sheet(1)
                .doReadSync();

        Map<Integer, List<AlongWayWaterTem>> alongWayWaterTemMapDiff = Maps.newHashMap();
        EasyExcel.read(file_dir_input)
                .registerReadListener(new AlongWayWaterTemReadListener(alongWayWaterTemMapDiff))
                .headRowNumber(4)
                //.head(AlongWayWaterTem.class)
                .sheet(1)
                .doReadSync();

        Iterator<Map.Entry<Integer, List<AlongWayWaterTem>>> iterator = alongWayWaterTemMap.entrySet().iterator();
        List<List<Double>> dataR = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<AlongWayWaterTem>> entry = iterator.next();
            Integer key = entry.getKey();
            List<AlongWayWaterTem> alongWayWaterTems = entry.getValue();
            List<AlongWayWaterTem> alongWayWaterTemsDiff = alongWayWaterTemMapDiff.get(key);
            List<Double> waterTems = Lists.newArrayList();
            for (AlongWayWaterTem alongWayWaterTem : alongWayWaterTems) {
                AlongWayWaterTem alongWayWaterComTem = alongWayWaterTemsDiff.stream()
                        .filter(item -> Objects.equals(item.getDistance(), alongWayWaterTem.getDistance()))
                        .findFirst().orElse(null);
                if (alongWayWaterComTem != null) {
                    waterTems.add(alongWayWaterTem.getWaterTem()- alongWayWaterComTem.getWaterTem());
                }
            }
            dataR.add(waterTems);
        }
        Set<Map.Entry<Integer, List<AlongWayWaterTem>>> entries = alongWayWaterTemMap.entrySet();
        List<List<String>> head = Lists.newArrayList();
        for (Map.Entry<Integer, List<AlongWayWaterTem>> entry : entries) {
            List<String> headItem = Lists.newArrayList();
            headItem.add(entry.getKey().toString());
            head.add(headItem);
        }
        List<List<Object>> data = Lists.newArrayList();
        //方差
        for (int i = 0; i < dataR.size(); i++) {
            List<Double> residualSqCollectS = dataR.get(i).stream()
                    .map(item -> Math.pow(item, 2))
                    .collect(Collectors.toList());
            double sum = residualSqCollectS.stream().mapToDouble(Double::doubleValue).sum();
            data.add(Arrays.asList("第" +(i+1)+ "天方差", sum));
        }

        ExcelWriter excelWriter = EasyExcel.write(rootDir + "/input/bbb.xlsx").build();
        WriteSheet excelWriterSheetR = EasyExcel.writerSheet(0, "水温沿程变化-残差").head(head).build();
        WriteSheet excelWriterSheetF = EasyExcel.writerSheet(1, "水温沿程变化-方差").build();
        excelWriter.write(transposeMatrix(dataR), excelWriterSheetR);
        excelWriter.write(data, excelWriterSheetF);
        excelWriter.finish();

    }

    public static <T> List<List<T>> transposeMatrix(List<List<T>> matrix) {
        return IntStream.range(0, matrix.get(0).size())
                .mapToObj(i -> matrix.stream()
                        .map(row -> row.get(i))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @Test
    public void hdSequenceWaterTest(){
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/input/河道模型对比数据模板.xlsx";
        List<List<String>> head = Lists.newArrayList();
        List<SequenceWaterTem> sequenceWaterTems = EasyExcel.read(file_dir_input, new SequenceWaterTemReadListener(head))
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();
        List<SequenceWaterTem> sequenceWaterComTems = EasyExcel.read(file_dir_input)
                .head(SequenceWaterTem.class)
                .headRowNumber(4)
                .sheet(0)
                .doReadSync();
        List<AlongWayWaterAlongResidualTem> sequenceWaterResidualTems = Lists.newArrayList();
        for (SequenceWaterTem sequenceWaterTem : sequenceWaterTems) {
            SequenceWaterTem sequenceWaterComTem = sequenceWaterComTems.stream()
                    .filter(item -> Objects.equals(item.getDay(), sequenceWaterTem.getDay()))
                    .findFirst().orElse(null);
            if (sequenceWaterComTem != null) {
                AlongWayWaterAlongResidualTem sequenceWaterTemResidual = new AlongWayWaterAlongResidualTem();
                sequenceWaterTemResidual.setDay(sequenceWaterTem.getDay());
                sequenceWaterTemResidual.setS1(sequenceWaterTem.getS1()-sequenceWaterComTem.getS1());
                sequenceWaterTemResidual.setS2(sequenceWaterTem.getS2()-sequenceWaterComTem.getS2());
                sequenceWaterResidualTems.add(sequenceWaterTemResidual);
            }
        }

        //方差逻辑
        List<Double> residualSqCollect = sequenceWaterResidualTems.stream()
                .map(item -> Math.pow(item.getS1(), 2))
                .collect(Collectors.toList());
        double sum = residualSqCollect.stream().mapToDouble(Double::doubleValue).sum();
        sum = sum/residualSqCollect.size();                     //方差结果
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);

        //方差逻辑
        List<Double> residualSqCollectS = sequenceWaterResidualTems.stream()
                .map(item -> Math.pow(item.getS2(), 2))
                .collect(Collectors.toList());
        double sum2 = residualSqCollectS.stream().mapToDouble(Double::doubleValue).sum();
        sum2 = sum2/residualSqCollect.size();                     //方差结果
        System.out.println("dischargeWaterTemperatureTest------------------"+sum);

        List<String> strings = head.get(1);
        List<String> strings1 = head.get(2);
        double sum3 = (sum+sum2);
        List<List<Object>> data = Arrays.asList(
                Arrays.asList(strings.get(0)+"方差", sum),
                Arrays.asList(strings1.get(0)+"方差", sum2),
                Arrays.asList("总方差", sum3)
        );
        ExcelWriter excelWriter = EasyExcel.write(rootDir + "/input/aaa.xlsx").build();
        WriteSheet excelWriterSheetR = EasyExcel.writerSheet(0, "断面水温变化-残差").head(head).build();
        WriteSheet excelWriterSheetF = EasyExcel.writerSheet(1, "断面水温变化-方差").build();
        excelWriter.write(sequenceWaterResidualTems, excelWriterSheetR);
        excelWriter.write(data, excelWriterSheetF);
        excelWriter.finish();
    }

}
