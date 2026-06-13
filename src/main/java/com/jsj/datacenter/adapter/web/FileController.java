package com.jsj.datacenter.adapter.web;

import com.github.pagehelper.PageInfo;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.infrastructure.common.entity.TableDataInfo;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import com.jsj.datacenter.application.FileItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/24
 */
@Api(tags = "文件接口")
@RestController()
@RequestMapping("file")
@EasyResponse
public class FileController {

    @Autowired
    private FileItemService fileItemService;

    @PostMapping("upload")
    @ApiOperation(value = "上传文件")
    public FileItemDTO upload(@RequestParam(value = "fileType") UploadFileType fileType, @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ServiceException("请选择文件");
        }
        return fileItemService.saveFileData(fileType, file);
    }

    @GetMapping("files")
    @ApiOperation(value = "获取某类型的所有上传历史")
    public TableDataInfo<FileItemDTO> queryFileList(
            @RequestParam UploadFileType fileType
    ) {
        return fileItemService.queryFileList(fileType);
    }

    @PostMapping("delete")
    @ApiOperation(value = "删除上传记录")
    public void delete(@RequestParam("fileKey") String fileKey) {
        fileItemService.removeByKey(fileKey);
    }

}
