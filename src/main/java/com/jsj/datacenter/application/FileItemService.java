package com.jsj.datacenter.application;

import com.jsj.datacenter.adapter.dto.response.FileItemDTO;
import com.jsj.datacenter.application.converter.FileItemConverter;
import com.jsj.datacenter.infrastructure.common.entity.TableDataInfo;
import com.jsj.datacenter.infrastructure.common.enums.UploadFileType;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import com.jsj.datacenter.infrastructure.common.helper.ExcelParser;
import com.jsj.datacenter.infrastructure.common.utils.SecurityUtils;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileItemService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${upload.excel}")
    private String excelExt;

    @Value("${upload.image}")
    private String imageExt;

    @Autowired
    private FileItemConverter converter;

    @Autowired
    private FileItemMapper fileItemMapper;

    public FileItemDTO saveImageFile(UploadFileType fileType, String fileName, BufferedImage bufferedImage) throws IOException {
        String fileKey = UUID.randomUUID().toString();

        // 处理文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        this.checkSuffix(suffix, fileType);
        String subDir = fileType.name().toLowerCase();
        // 创建存储目录
        Path storagePath = Paths.get("uploads", subDir).toAbsolutePath().normalize();
        // 使用NIO方式创建目录
        Files.createDirectories(storagePath);
        FileItemPO fileItem = new FileItemPO();
        Path targetPath = storagePath.resolve(fileKey + suffix);

        OutputStream outputStream = Files.newOutputStream(targetPath, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        ImageIO.write(bufferedImage, fileType.getFileType(), outputStream);
        fileItem.setPath(targetPath.toString());
        fileItem.setFileKey(fileKey)
                .setFilename(fileName)
                .setExtension(suffix)
                .setFileType(fileType)
                .setDeleted(0)
                .setCreateBy(SecurityUtils.getLoginUserId())
                .setCreateTime(new Date());
        fileItem.setUrlPath("/uploads/"+subDir+"/"+fileKey + suffix);
        fileItemMapper.insert(fileItem);
        return converter.toDto(fileItem);
    }

    public FileItemDTO saveFileData(UploadFileType fileType, String fileName, InputStream inputStream) {
        //String filename = file.getName();
        String fileKey = UUID.randomUUID().toString();

        // 处理文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        this.checkSuffix(suffix, fileType);
        String subDir = fileType.name().toLowerCase();
        // 创建存储目录
        Path storagePath = Paths.get("uploads", subDir).toAbsolutePath().normalize();
        FileItemPO fileItem = new FileItemPO();
        try {
            // 使用NIO方式创建目录
            Files.createDirectories(storagePath);

            // 使用Path对象直接操作文件路径
            Path targetPath = storagePath.resolve(fileKey + suffix);
            // 使用try-with-resources确保流关闭
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            fileItem.setUrlPath("/uploads/"+subDir+"/"+fileKey + suffix);
            // 更新数据库存储路径
            fileItem.setPath(targetPath.toString());

        } catch (IOException e) {
            log.error("文件存储失败: {}", e.getMessage());
            throw new ServiceException("文件存储失败");
        }

        fileItem.setFileKey(fileKey)
                .setFilename(fileName)
                .setExtension(suffix)
                .setFileType(fileType)
                .setDeleted(0)
                .setCreateBy(SecurityUtils.getLoginUserId())
                .setCreateTime(new Date());

        fileItemMapper.insert(fileItem);
        return converter.toDto(fileItem);
    }

    @Transactional
    public FileItemDTO saveFileData(UploadFileType fileType, File file) {
        try {
            return this.saveFileData(fileType, file.getName(), Files.newInputStream(file.toPath()));
        } catch (IOException e) {
            log.error("文件存储失败: {}", e.getMessage());
            throw new ServiceException("文件存储失败");
        }
    }

    @Transactional
    public FileItemDTO saveFileData(UploadFileType fileType, MultipartFile file) {
        try {
            return this.saveFileData(fileType, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            log.error("文件存储失败: {}", e.getMessage());
            throw new ServiceException("文件存储失败");
        }
        /*String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String fileKey = UUID.randomUUID().toString();

        // 验证文件基础信息
        if (filename == null || filename.isEmpty() || contentType == null) {
            throw new ServiceException("无效的文件格式");
        }

        // 处理文件后缀
        String suffix = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        this.checkSuffix(suffix, fileType);
        // 创建存储目录
        Path storagePath = Paths.get("uploads", fileType.name().toLowerCase()).toAbsolutePath().normalize();
        FileItemPO fileItem = new FileItemPO();
        try {
            // 使用NIO方式创建目录
            Files.createDirectories(storagePath);

            // 使用Path对象直接操作文件路径
            Path targetPath = storagePath.resolve(fileKey + suffix);

            // 使用try-with-resources确保流关闭
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 更新数据库存储路径
            fileItem.setPath(targetPath.toString());

        } catch (IOException e) {
            log.error("文件存储失败: {}", e.getMessage());
            throw new ServiceException("文件存储失败");
        }

        fileItem.setFileKey(fileKey)
                .setFilename(filename)
                .setExtension(suffix)
                .setFileType(fileType)
                .setDeleted(0)
                .setCreateBy(SecurityUtils.getLoginUserId())
                .setCreateTime(DateUtils.getTime());

        fileItemMapper.insert(fileItem);
        return converter.toDto(fileItem);*/

    }

    public TableDataInfo<FileItemDTO> queryFileList(UploadFileType fileType) {
        List<FileItemPO> poList = fileItemMapper.queryFiles(fileType);
        return TableDataInfo.getDataTable(poList, FileItemDTO.class);
    }

    public void removeByKey(String fileKey) {
        fileItemMapper.removeFile(fileKey);
    }

    public <T> List<T> parse(String fileKey, Class<T> clazz) {
        if (null == clazz) {
            throw new ServiceException("该类型文件不支持解析");
        }
        FileItemPO fileItem = fileItemMapper.getFile(fileKey);
        if (fileItem == null) {
            throw new ServiceException("文件不存在");
        }
        if (!fileItem.getFileType().getClazz().equals(clazz)) {
            throw new ServiceException("文件类型不匹配");
        }
        File file = new File(fileItem.getPath());
        if (!file.exists()) {
            throw new ServiceException("文件上传失败");
        }
        try {
            ExcelParser<T> parser = new ExcelParser<>();
            return parser.parse(file, clazz);
        } catch (Exception e) {
            log.error("Excel parse error: {}", e.getMessage());
            throw new ServiceException("Excel解析失败，请上传正确的模板");
        }
    }

    public void downloadFile(String fileKey, HttpServletResponse response) {
        FileItemPO file = fileItemMapper.getFile(fileKey);
        if (file == null) {
            throw new ServiceException("文件不存在");
        }

        File targetFile = new File(file.getPath());
        if (!targetFile.exists()) {
            throw new ServiceException("文件已丢失");
        }

        try {
            // 将文件名UrlEncode
            String fileName = URLEncoder.encode(file.getFilename(), "UTF-8");
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


    private void checkSuffix(String suffix, UploadFileType fileType) {
        String tmpSuffix = suffix;
        if (suffix.startsWith(".")) {
            tmpSuffix = suffix.substring(1);
        }
        if ("excel".equals(fileType.getFileType()) && !excelExt.contains(tmpSuffix)) {
            throw new ServiceException("请上传Excel模板格式文件");
        }
        if ("image".equals(fileType.getFileType()) && !imageExt.contains(tmpSuffix)) {
            throw new ServiceException("请上传图片文件");
        }
    }
}

