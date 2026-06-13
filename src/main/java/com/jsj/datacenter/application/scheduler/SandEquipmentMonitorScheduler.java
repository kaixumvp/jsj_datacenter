package com.jsj.datacenter.application.scheduler;

import com.jsj.datacenter.application.environment.service.SandEquipmentMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 砂石设备监测数据定时任务调度器
 */
@Slf4j
@Component
public class SandEquipmentMonitorScheduler {
    
    @Autowired
    private SandEquipmentMonitorService monitorService;
    
    /**
     * 每15分钟执行一次同步任务
     * Cron表达式: 秒 分 时 日 月 周
     * 0 0/15 * * * ? 表示每15分钟的第0秒执行
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void syncSandEquipmentData() {
        log.info("========== 定时任务触发：砂石设备监测数据同步 ==========");
        
        try {
            int savedCount = monitorService.syncMonitorData();
            
            if (savedCount > 0) {
                log.info("定时任务执行成功，共同步 {} 条数据", savedCount);
            } else {
                log.warn("定时任务执行完成，但没有同步到数据");
            }
        } catch (Exception e) {
            log.error("定时任务执行失败: {}", e.getMessage(), e);
        }
    }
}
