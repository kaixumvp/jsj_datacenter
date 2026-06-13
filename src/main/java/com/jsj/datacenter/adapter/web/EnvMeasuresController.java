package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresDTO;
import com.jsj.datacenter.adapter.dto.envmeasures.EnvMeasuresListDTO;
import com.jsj.datacenter.application.envmeasures.service.EnvMeasuresService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Api(tags = "环保措施相关接口")
@RestController
@EasyResponse
@RequestMapping("env-measures")
public class EnvMeasuresController {

    @Autowired
    private EnvMeasuresService envMeasureService;

    @ApiOperation("获取环保措施列表")
    @GetMapping("list")
    public EnvMeasuresListDTO getEnvMeasures() {
        return envMeasureService.getAllEnvMeasures();
    }

    @ApiOperation("添加环保措施")
    @PostMapping("add")
    public void addEnvMeasure(@RequestBody @Validated EnvMeasuresDTO measure) {
        envMeasureService.createEnvMeasures(measure);
    }

    @ApiOperation("更新环保措施")
    @PostMapping("update")
    public void updateEnvMeasure(@RequestBody @Validated EnvMeasuresDTO measure) {
        envMeasureService.updateEnvMeasures(measure);
    }

    @ApiOperation("获取环保措施")
    @GetMapping("get/{id}")
    public EnvMeasuresDTO getEnvMeasure(@PathVariable Long id) {
        return envMeasureService.getEnvMeasures(id);
    }

    @ApiOperation("删除环保措施")
    @GetMapping("delete/{id}")
    public void deleteEnvMeasure(@PathVariable Long id) {
        envMeasureService.deleteEnvMeasures(id);
    }
}
