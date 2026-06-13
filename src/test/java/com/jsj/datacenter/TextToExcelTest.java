package com.jsj.datacenter;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.jsj.datacenter.adapter.temp.AlongWayWaterTem;
import com.jsj.datacenter.adapter.temp.HeaderRepeatWriterHandler;
import com.jsj.datacenter.adapter.temp.SequenceWaterTem;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class TextToExcelTest {
    String rootDir = System.getProperty("user.dir");

    @Test
    public void twdisProfileTest() throws IOException {
        String file_dir_input = rootDir + "/script/ricen/Output/Profile/Ice/twdis.dat";
        Path path = Paths.get(file_dir_input);
        List<String> dataLines = Files.readAllLines(path);
        String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        dataLines.get(0);
        dataLines.get(1);
        dataLines.remove(0);
        dataLines.remove(0);
        TreeMap<Integer, String> headList = new TreeMap<>();
        List<AlongWayWaterTem> tems = Lists.newArrayList();
        for (int i = 1; i <= dataLines.size(); i++) {
            String line = dataLines.get(i - 1);
            System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
            if (Pattern.matches(pattern, line)){
                System.out.println(line.trim().replaceAll("\\s{2,}", "-"));
                String trimLine = line.trim();
                String[] items = trimLine.split("\\s{2,}");
                AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
                alongWayWaterTem.setDistance(Double.parseDouble(items[0]));
                alongWayWaterTem.setWaterTem(Double.parseDouble(items[1]));
                alongWayWaterTem.setAirTem(Double.parseDouble(items[2]));
                tems.add(alongWayWaterTem);
                System.out.println(JSON.toJSONString(alongWayWaterTem));
            } else  {
                System.out.println(line);
                headList.put(i, line);
            }
        }
        String file_dir_out = rootDir + "/script/ricen/Output/Profile/Ice/out.xlsx";
        Path path_out = Paths.get(file_dir_out);
        EasyExcel.write(path_out.toFile(), AlongWayWaterTem.class)
                .inMemory(true)
                .sheet("沿程水温")
                .registerWriteHandler(new HeaderRepeatWriterHandler(headList))
                .doWrite(tems);

    }

    @Test
    public void executorDataHandle() throws IOException {
        String file_dir_out = rootDir + "/script/ricen/Output/Profile/Ice/out.xlsx";
        Path path_out = Paths.get(file_dir_out);
        ExcelWriter excelWriter = EasyExcel.write(path_out.toFile()).inMemory(true).build();
        //this.handleTimeSeq(excelWriter);
        this.handleProfile(excelWriter);
        excelWriter.finish();
    }

    private void handleTimeSeq(ExcelWriter excelWriter) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/script/ricen/Output/Time Sequence/Ice/twtm.dat";
        Path path = Paths.get(file_dir_input);
        List<String> dataLines = Files.readAllLines(path);
        String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        dataLines.get(0);
        dataLines.get(1);
        dataLines.get(2);
        dataLines.remove(0);
        dataLines.remove(0);
        dataLines.remove(0);
        List<SequenceWaterTem> tems = Lists.newArrayList();
        for (int i = 1; i <= dataLines.size(); i++) {
            String line = dataLines.get(i - 1);
            System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
            if (Pattern.matches(pattern, line)){
                System.out.println(line.trim().replaceAll("\\s+", "-"));
                String trimLine = line.trim();
                String[] items = trimLine.split("\\s+");
                SequenceWaterTem sequenceWaterTem = new SequenceWaterTem();
                sequenceWaterTem.setDay(Double.parseDouble(items[0]));
                sequenceWaterTem.setS1(Double.parseDouble(items[1]));
                sequenceWaterTem.setS2(Double.parseDouble(items[2]));
                tems.add(sequenceWaterTem);
            } else  {
            }
        }
        WriteSheet sheet = EasyExcel.writerSheet(0, "断面水温").build();
        excelWriter.write(tems, sheet);
       /* EasyExcel.write(path_out.toFile(), SequenceWaterTem.class)
                .inMemory(true)
                .sheet("断面水温")
                .doWrite(tems);*/
    }

    private void handleProfile(ExcelWriter excelWriter) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String file_dir_input = rootDir + "/script/ricen/Output/Profile/Ice/twdis.dat";
        Path path = Paths.get(file_dir_input);
        List<String> dataLines = Files.readAllLines(path);
        int[] indices = IntStream.range(0, dataLines.size())
                .filter(i -> dataLines.get(i).startsWith("Zone"))
                .toArray();
        String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        String regex = "\\s+";
        WriteSheet sheet = EasyExcel.writerSheet(1, "沿程水温")
                .head(AlongWayWaterTem.class)
                .build();
        for (int i = 0; i < indices.length; i++) {
            int indexStart = indices[i];
            int indexEnd;
            if(i == indices.length-1){
                indexEnd = dataLines.size();
            } else {
                indexEnd = indices[i+1];
            }
            List<String> subList = dataLines.subList(indexStart, indexEnd);
            List<Object> tems = Lists.newArrayList();
            List<List<String>> head = Lists.newArrayList();
            for (int j = 1; j <= subList.size(); j++) {
                String line = subList.get(j - 1);
                System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
                if (Pattern.matches(pattern, line)){
                    System.out.println(line.trim().replaceAll("\\s+", "-"));
                    String trimLine = line.trim();
                    String[] items = trimLine.split("\\s+");
                    AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
                    alongWayWaterTem.setDistance(Double.parseDouble(items[0]));
                    alongWayWaterTem.setWaterTem(Double.parseDouble(items[1]));
                    alongWayWaterTem.setAirTem(Double.parseDouble(items[2]));
                    tems.add(alongWayWaterTem);
                } else  {
                    String[] split = line.split(regex);
                    for(String item: split){
                        List<String> headItem = Lists.newArrayList();
                        headItem.add(item);
                        head.add(headItem);
                    }
                }
            }
            WriteTable writeTable = EasyExcel.writerTable(i).head(head).build();
            excelWriter.write(tems, sheet,  writeTable);
        }

        /*String pattern = "^(?:\\s*[+-]?\\d+\\.?\\d*\\s*)+$";
        dataLines.get(0);
        dataLines.get(1);
        dataLines.remove(0);
        dataLines.remove(0);
        TreeMap<Integer, String> headList = new TreeMap<>();
        List<AlongWayWaterTem> tems = Lists.newArrayList();
        for (int i = 1; i <= dataLines.size(); i++) {
            String line = dataLines.get(i - 1);
            System.out.println("匹配及结果：" + Pattern.matches(pattern, line));
            if (Pattern.matches(pattern, line)){
                System.out.println(line.trim().replaceAll("\\s+", "-"));
                String trimLine = line.trim();
                String[] items = trimLine.split("\\s+");
                AlongWayWaterTem alongWayWaterTem = new AlongWayWaterTem();
                alongWayWaterTem.setDistance(Double.parseDouble(items[0]));
                alongWayWaterTem.setWaterTem(Double.parseDouble(items[1]));
                alongWayWaterTem.setAirTem(Double.parseDouble(items[2]));
                tems.add(alongWayWaterTem);
            } else  {
                headList.put(i, line);
            }
        }
        WriteSheet sheet = EasyExcel.writerSheet(1, "沿程水温")
                .head(AlongWayWaterTem.class)
                .registerWriteHandler(new HeaderRepeatWriterHandler(headList))
                .build();

        excelWriter.write(tems, sheet);*/
    }

    @Test
    public void test(){
        String regex = ",\\s+|\\s+";
        //String regex = "[,\\s]|=\\s*=";
        String a = "     1.001,    4.00";
        String b = "    0.00  7.3000  6.9700";
        String[] split = b.trim().split(regex);
        System.out.println(split.length);
    }

    @Test
    public void pytest() throws IOException {
        String bth_file = "/script/xl/XL-weir/bth.npt";
        String elev_file = "/script/xl/XL-weir/elevation.opt";
        String prf_file = "/script/xl/XL-weir/prf.opt";
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/坝前温度随时间变化二维云图(prf).py";
        String outPath = rootDir+"/uploads/river/bqsw/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                rootDir + bth_file,
                rootDir + elev_file,
                rootDir + prf_file,
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
    }

    @Test
    public void pyCXSWTest() throws IOException {
        String rootDir = System.getProperty("user.dir");
        String bth_file = "/script/xl/XL-weir/snp.opt";
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/垂向温度结构结果整理输出.py";
        String outPath = rootDir+"/uploads/river/cxwd/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                rootDir + bth_file,
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    @Test
    public void xxPyTest() throws IOException, InterruptedException {
        //获取项链路径
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/下泄水温绘制.py";
        String outPath = rootDir+"/uploads/river/xx/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }

        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                rootDir+"/script/xl/XL-weir/two_130.opt",
                rootDir+"/uploads/river/xx/"+startTime+".png",
                rootDir+"/uploads/river/xx/"+startTime+".xlsx");
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束
        process.waitFor();
    }

    @Test
    public void erPytest() throws IOException, InterruptedException {
        String rootDir = System.getProperty("user.dir");
        String pythonExecutable = "python"; // 或者 "python.exe" 在Windows上
        // 指定Python脚本的路径
        String scriptPath = rootDir+"/script/xl/python/二维云图绘制.py";
        String outPath = rootDir+"/uploads/river/yt/";
        File file = new File(outPath);
        if (file.mkdirs()) {
            System.out.println("创建输出文件路径："+ outPath);
        }
        long startTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        // 创建ProcessBuilder实例并设置命令
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, scriptPath,
                rootDir+"/script/xl/XL-weir/cpl.opt",
                outPath);
        builder.redirectErrorStream(true);
        // 启动进程
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // 等待进程结束
        process.waitFor();
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        /*Process p = Runtime.getRuntime().exec(rootDir+"/script/xl/XL-weir/w2_v4_64.exe");
        p.waitFor();*/ // 等待进程结束
        //如何执行
        /*String rootDir = System.getProperty("user.dir");
        String main_flow = rootDir + "/script/xl/XL-weir/w2_v4_64.exe";
        // 创建ProcessBuilder
       // ProcessBuilder pb = new ProcessBuilder(main_flow);
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "/B", main_flow);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        File file = new File(main_flow);
        pb.directory(file.getParentFile());
        Process process = pb.start();

        // 获取输出流（用于向exe发送输入）
        OutputStream outputStream = process.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        // 等待程序执行完成
        int exitCode = process.waitFor();
        System.out.println("程序退出代码: " + exitCode);
        writer.close();*/

        //Runtime.getRuntime().exec("taskkill /f /im w2_v4_64.exe");

        ProcessBuilder pb = new ProcessBuilder("wscript", rootDir+"/script/xl/XL-weir-wdq/"+"invis.vbs");
        File vbsFile = new File(rootDir+"/script/xl/XL-weir-wdq/"+"invis.vbs");
        pb.redirectErrorStream(true); // 合并错误流和输出流
        pb.directory(vbsFile.getParentFile());
        try {
            Process process = pb.start();

            process.waitFor();
            // 读取输出（同上）
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Runtime.getRuntime().exec("wscript.exe " + rootDir+"/script/xl/XL-weir" + "/invis.vbs");*/
    }

    @Test
    public void test2() {
        for (int i = 1; i < 365; i++) {
            System.out.print(i*6+" ");
        }
    }
}
