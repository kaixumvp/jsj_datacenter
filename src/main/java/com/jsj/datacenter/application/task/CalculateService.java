package com.jsj.datacenter.application.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CalculateService {
    private static String path;

    @Value("${calculate.shell.path}")
    public void setPath(String shellPath) {
        path = shellPath;
    }


    public void replaceParam(TaskParam param) {
        log.debug("start replace params");
        log.debug("aaa");
        log.debug("bbb");
    }

    public void executeScript() throws Exception {
        List<String> scriptPath = getScriptPath();
        try {
            for (String s : scriptPath) {
                log.debug("start execute script:{}",s);
                String command ;
                String absolutePath;
                ProcessBuilder processBuilder;
                if (s.endsWith(".py")){
                    command = "python";
                    absolutePath = path+"/python";
                    processBuilder= new ProcessBuilder(command,s).directory(new File(absolutePath));
                }else {
                    absolutePath = path;
                    processBuilder = new ProcessBuilder("cmd","/c",s).directory(new File(absolutePath));
                }

                Process process = processBuilder.start();
                boolean error = false;
                // 读取标准输出
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Script Output: {}", line);
                    }
                }

                // 读取错误输出
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        error = true;
                        log.error("Script Error: {}", errorLine);
                    }
                }

                int exitCode = process.waitFor();
                log.info("Script exited with code: {}", exitCode);
                if (error){
                    throw new Exception("execute script:"+scriptPath+"error");
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute script: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<String> getScriptPath() {
        List<String> scripts = new ArrayList<>();
        String bq = "坝前温度随时间变化二维云图(prf).py";
        String cx = "垂向温度结构结果整理输出.py";
        String ew =  "二维云图绘制.py";
        String ricen = "RICEN-1D.exe";
        scripts.add(bq);
        scripts.add(cx);
        scripts.add(ew);
        scripts.add(ricen);
        return scripts;
    }

}
