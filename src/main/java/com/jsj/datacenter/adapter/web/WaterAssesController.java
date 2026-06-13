package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.waschema.request.WaSchemaAddRequest;
import com.jsj.datacenter.adapter.dto.waschema.response.WaFileParseResult;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.application.task.CalculateService;
import com.jsj.datacenter.application.task.CalculateTask;
import com.jsj.datacenter.application.task.CalculateTaskFactory;
import com.jsj.datacenter.application.task.TaskParam;
import com.jsj.datacenter.application.waschema.service.WaSchemaService;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "水环境评估接口")
@RestController()
@RequestMapping("water-asses")
@EasyResponse
public class WaterAssesController {

    @Autowired
    WaSchemaService waSchemaService;
    @Autowired
    FileItemService fileItemService;
    @Autowired
    CalculateService calculateService;

    @PostMapping("/add")
    @ApiOperation(value = "新增评估方案")
    public void add(WaSchemaAddRequest request) {
        waSchemaService.add(request);
    }

    @PostMapping("/parse-file")
    @ApiOperation(value = "Excel文件解析")
    public WaFileParseResult parse(String fileKey, UploadFileType fileType) {
        return waSchemaService.parse(fileKey, fileType.getClazz());
    }

    @PostMapping("/start-calculate")
    @ApiOperation(value = "开始计算")
    public void startCalculate(@RequestParam("schemaId") Integer schemaId) {
        TaskParam taskParam = waSchemaService.getTaskParam(schemaId);
        if (taskParam!=null){
            CalculateTaskFactory.getInstance().addTask(schemaId.toString(), new CalculateTask(taskParam,calculateService));
        }
    }

    @GetMapping("/get-task-process")
    @ApiOperation(value = "获取任务进度")
    public CalculateTaskFactory.TaskProcess getTaskProcess(@RequestParam("schemaId") Integer schemaId) {
        return CalculateTaskFactory.getInstance().getTaskProcess(schemaId.toString());
    }
}
