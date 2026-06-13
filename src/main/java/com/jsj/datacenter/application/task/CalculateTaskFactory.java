package com.jsj.datacenter.application.task;

import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CalculateTaskFactory implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(CalculateTaskFactory.class);
    @Getter
    private static CalculateTaskFactory instance = new CalculateTaskFactory();

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private CalculateTaskFactory() {
    }

    private Map<String, CalculateTask> taskMap = new ConcurrentHashMap<>();

    public void addTask(String key, CalculateTask task) {
        taskMap.put(key, task);
        threadPool.submit(task);
    }

    public CalculateTask getTask(String key) {
        return taskMap.get(key);
    }

    @PostConstruct
    public void init() {
        threadPool.submit(this);
    }

    public TaskProcess getTaskProcess(String key) {
        CalculateTask task = taskMap.get(key);
        if (task == null) {
            return null;
        }
        TaskProcess taskProcess = new TaskProcess();
        taskProcess.setStatus(task.getStatus()==null?null:task.getStatus().getDescription());
        taskProcess.setProgress(task.getProgress());
        return taskProcess;
    }



    @Override
    public void run() {
        System.out.println("task factory start...");
        while (true) {
            for (Map.Entry<String, CalculateTask> entry : taskMap.entrySet()) {
                CalculateTask task = entry.getValue();
                if (task.getProgress() == 100 || task.getStatus()== TaskStatus.INTERRUPTED) {
                    taskMap.remove(entry.getKey());
                }
            }
            try {
                Thread.sleep(3600000);//每小时执行一次
            } catch (InterruptedException e) {
                log.error("sleep error", e);
            }
        }
    }
    @Data
    public class TaskProcess{
        private int progress;
        private String status;;
    }
}
