package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.application.FileItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/27
 */
@Api(tags = "文件解析接口")
@RestController()
@RequestMapping("parse")
@EasyResponse
public class ParseController {
    @Autowired
    FileItemService fileItemService;

    @PostMapping("excel")
    @ApiOperation(value = "Excel文件解析")
    public List<?> parse(String fileKey, UploadFileType fileType) {
        return fileItemService.parse(fileKey, fileType.getClazz());
    }
}
