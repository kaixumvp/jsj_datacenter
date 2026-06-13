package com.jsj.datacenter;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.google.common.collect.Lists;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WaterTemperatureReportTest {

    @Test
    public void generateBoundaryConditionParametersTest() throws IOException {
        WaterTemperaturePlan waterTemperaturePlan1 = new WaterTemperaturePlan();
        waterTemperaturePlan1.setSpread(0.8);
        waterTemperaturePlan1.setBeta(0.5);
        waterTemperaturePlan1.setExh(0.9);
        WaterTemperaturePlan waterTemperaturePlan2 = new WaterTemperaturePlan();
        waterTemperaturePlan2.setMecovertcoe(0.6);
        waterTemperaturePlan2.setSolarcoe(0.9);
        waterTemperaturePlan2.setWindcoe(1.0);
        String rootDir = System.getProperty("user.dir");
        String docxTemplate = rootDir+"/script/template/旭龙水温分析报告模板.docx";
        LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy ();
        Configure config = Configure.builder()
                .bind("reservoirs", policy).bind("riverCourses", policy).build();
        // 1. 编译模板
        XWPFTemplate template = XWPFTemplate.compile(docxTemplate,config);
        // 2. 填充数据（Map 或对象）
        Map<String, Object> data = new HashMap<>();
        data.put("timeStamp", Instant.now().toEpochMilli());
        data.put("analysisTime", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        data.put("creator", "admin");
        data.put("reservoirs", Lists.newArrayList(waterTemperaturePlan1));
        data.put("riverCourses", Lists.newArrayList(waterTemperaturePlan2));
        //data.put("employees", List.of(Map.of("name", "李四", "age", 30)));
        XWPFTableRow row  = template.getXWPFDocument().getAllTables().get(0).getRow(3);
        String text = row.getCell(2).getText();
        // 3. 渲染并输出
        template.render(data);
        template.writeToFile(rootDir+"/script/template"+"/output.docx");
        template.close();
    }
}
