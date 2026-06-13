package com.jsj.datacenter.adapter.web;

import com.jsj.datacenter.application.FileItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Api(tags = "下载接口")
@RestController
@RequestMapping("download")
public class DownloadController {

    @Autowired
    private FileItemService fileItemService;

    @GetMapping("file/{key}")
    @ApiOperation(value = "下载文件")
    public void queryFileList(@PathVariable("key") String fileKey, HttpServletResponse response) {
        fileItemService.downloadFile(fileKey, response);
    }


}
