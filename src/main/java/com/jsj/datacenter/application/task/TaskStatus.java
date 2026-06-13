package com.jsj.datacenter.application.task;
public enum TaskStatus {
    NOT_STARTED("未开始"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    INTERRUPTED("已中断");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
