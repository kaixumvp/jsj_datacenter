package com.jsj.datacenter.application.task;

import com.jsj.datacenter.application.waschemaresult.service.WaSchemaResultService;
import com.jsj.datacenter.infrastructure.common.utils.TraceUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;



@Slf4j
@Data
public class CalculateTask implements Runnable {
    private volatile TaskStatus status = TaskStatus.NOT_STARTED;
    private volatile int progress = 0;

    private CalculateService calculateService;

    private WaSchemaResultService resultService;
    private TaskParam param;

    public CalculateTask(TaskParam param,CalculateService calculateService) {
        this.param = param;
        this.calculateService = calculateService;
    }

    @Override
    public void run() {
        status = TaskStatus.RUNNING;
        String traceId = TraceUtil.generateTraceId();
        TraceUtil.putTraceId(traceId);
        try {
            log.info("start running calculate task" );
            log.info("traceId: {}", traceId);
            log.debug("starting...");
            calculateService.replaceParam(param);
            calculateService.executeScript();
            status = TaskStatus.COMPLETED;
            log.debug("finished...");
        } catch (Exception e) {
            status = TaskStatus.INTERRUPTED;
            log.error("error: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }finally {
            TraceUtil.clearTraceId();
        }
    }
}
