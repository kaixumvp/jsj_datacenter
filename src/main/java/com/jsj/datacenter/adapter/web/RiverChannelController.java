package com.jsj.datacenter.adapter.web;

import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateResult;
import com.jsj.datacenter.adapter.dto.river.RiverCalculateVO;
import com.jsj.datacenter.application.FileItemService;
import com.jsj.datacenter.application.watertemperature.domain.WaterTemperaturePlan;
import com.jsj.datacenter.application.watertemperature.result.PlanParamsResult;
import com.jsj.datacenter.application.watertemperature.result.RiverExecuteLog;
import com.jsj.datacenter.application.watertemperature.service.RiverService;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.rmi.server.ServerCloneException;
import java.util.List;

@Api(tags = "水库模型接口")
@RestController
@RequestMapping("river")
@EasyResponse
@Slf4j
public class RiverChannelController {

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private RiverService riverService;

    @PostMapping("upload")
    @ApiOperation(value = "上传文件")
    public FileItemDTO upload (@RequestParam(value = "fileType") UploadFileType fileType, @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            throw new ServiceException("请选择文件");
        }
        FileItemDTO fileItemDTO = fileItemService.saveFileData(fileType, file);
        fileItemDTO.setRecords(riverService.excelParse(file, fileType));
        return fileItemDTO;
    }

    @GetMapping("download")
    @ApiOperation(value = "下载文件")
    public void queryFileList(HttpServletResponse response) {
        try {
            String rootDir = System.getProperty("user.dir");


            File targetFile = new File(rootDir + "/script/template/水库模型对比数据模板.xlsx");
            // 将文件名UrlEncode
            String fileName = URLEncoder.encode("river.xlsx", "UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");
            response.setContentLength((int) targetFile.length());

            Files.copy(targetFile.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new ServiceException("文件下载失败");
        }
    }

    @PostMapping("calculate")
    @ApiOperation(value = "水库计算")
    public JSONObject riverCalculate(@RequestBody RiverCalculateVO riverCalculate) throws IOException, InterruptedException {
        riverCalculate.setPlanName("水库模型");
        riverCalculate.setPlanType(1);
        WaterTemperaturePlan waterTemperaturePlan;
        try {
            waterTemperaturePlan = riverService.executePy(riverCalculate);
        } catch (Exception e){
            throw new ServiceException("水库模型执行任务创建失败");
        }
        return JSONObject.of("planId", waterTemperaturePlan.getId());
    }

    @PostMapping("obtainCalculate")
    @ApiOperation(value = "获取计算结果")
    public RiverCalculateResult obtainCalculate(@RequestParam("planId") Integer planId) {
        return riverService.obtainCalculate(planId);
    }

    @PostMapping("obtainTaskLog")
    @ApiOperation(value = "获取水库日志记录")
    public RiverExecuteLog obtainTaskLog(@RequestParam("planId") Integer planId) throws IOException {
        return riverService.queryTaskLog(planId);
    }

    @PostMapping("obtainTaskParam")
    @ApiOperation(value = "获取参数详情接口")
    public PlanParamsResult obtainTaskParam(@RequestParam(value = "planId", required = false) Integer planId, @RequestParam(value = "planType", required = false) Integer planType){
        if(planId == null && planType == null) {
            planType = 1;
        }
        return riverService.getPlanParams(planId, planType);
    }
}
