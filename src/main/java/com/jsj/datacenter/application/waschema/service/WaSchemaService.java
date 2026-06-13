package com.jsj.datacenter.application.waschema.service;

import com.jsj.datacenter.adapter.dto.waschema.request.SchemaParam;
import com.jsj.datacenter.adapter.dto.waschema.request.WaSchemaAddRequest;
import com.jsj.datacenter.adapter.dto.waschema.response.WaFileParseResult;
import com.jsj.datacenter.application.task.TaskParam;
import com.jsj.datacenter.application.waschema.domain.WaSchema;
import com.jsj.datacenter.application.waschema.mapper.WaSchemaMapper;
import com.jsj.datacenter.application.waschemadetail.domain.WaSchemaDetail;
import com.jsj.datacenter.application.waschemadetail.service.WaSchemaDetailService;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import com.jsj.datacenter.infrastructure.common.helper.ExcelParser;
import com.jsj.datacenter.infrastructure.common.helper.FileParser;
import com.jsj.datacenter.infrastructure.common.helper.ParserUtil;
import com.jsj.datacenter.infrastructure.fileitem.mapper.FileItemMapper;
import com.jsj.datacenter.infrastructure.fileitem.po.FileItemPO;
import com.jsj.datacenter.infrastructure.vo.annotaction.WaDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class WaSchemaService {

    @Autowired
    WaSchemaMapper waSchemaMapper;

    @Autowired
    FileItemMapper fileItemMapper;

    @Autowired
    WaSchemaDetailService waSchemaDetailService;

    @Transactional(rollbackFor = Exception.class)
    public void add(WaSchemaAddRequest request) {
        log.info("wa schema service add called");
        WaSchema waSchema = new WaSchema();
        waSchema.setName(request.getName());
        waSchema.setType(request.getType());
        waSchemaMapper.insert(waSchema);
        List<SchemaParam> params = request.getParam();
        List<WaSchemaDetail> schemaDetails = new ArrayList<>();
        for (SchemaParam param : params) {
            WaSchemaDetail waSchemaDetail = new WaSchemaDetail();
            BeanUtils.copyProperties(param, waSchemaDetail);
            waSchemaDetail.setId(waSchema.getId());
            schemaDetails.add(waSchemaDetail);
        }
        waSchemaDetailService.saveBatch(schemaDetails);
        log.info("wa schema service add finish");
    }

    public TaskParam getTaskParam(Integer schemaId) {
        WaSchema waSchema = waSchemaMapper.selectById(schemaId);
        if (waSchema == null) {
            return null;
        }
        TaskParam taskParam = new TaskParam();
        BeanUtils.copyProperties(waSchema, taskParam);
        waSchemaDetailService.getBySchemaId(schemaId).forEach(waSchemaDetail -> {
            taskParam.getWaSchemaDetails().add(waSchemaDetail);
        });
        return taskParam;
    }

    public <T> WaFileParseResult parse(String fileKey, Class<T> clazz) {
        if (null == clazz) {
            throw new ServiceException("该类型文件不支持解析");
        }
        WaFileParseResult fileParseResult = new WaFileParseResult();
        FileItemPO fileItem = fileItemMapper.getFile(fileKey);
        if (fileItem == null) {
            throw new ServiceException("文件不存在");
        }
        BeanUtils.copyProperties(fileItem, fileParseResult);
        if (!fileItem.getFileType().getClazz().equals(clazz)) {
            throw new ServiceException("文件类型不匹配");
        }
        File file = new File(fileItem.getPath());
        if (!file.exists()) {
            throw new ServiceException("文件上传失败");
        }
        try {

            FileParser<T> parser = ParserUtil.getParser(fileItem.getFileType().getFileType());
            List<T> parse = parser.parse(file, clazz);
            fileParseResult.setData(parse);
            getDate(parse, clazz, fileParseResult);
            return fileParseResult;
        } catch (Exception e) {
            log.error("File parse error: {}", e.getMessage());
            throw new ServiceException("File解析失败，请上传正确的模板");
        }
    }


    private <T> void getDate(List<T> parse, Class<T> clazz, WaFileParseResult fileParseResult) throws IllegalAccessException {
        if (parse == null || parse.isEmpty()) {
            return;
        }
        List<String> dateList = new ArrayList<>();
        String pattern = "";
        for (T t : parse) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                WaDate annotation = declaredField.getAnnotation(WaDate.class);
                if (annotation != null) {
                    if (pattern.isEmpty()) {
                        pattern = annotation.pattern();
                    }
                    Object o = declaredField.get(t);
                    if (o instanceof String) {
                        dateList.add((String) o);
                    }
                }
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        dateList.sort(Comparator.comparing(o -> LocalDate.parse(o, formatter)));
        fileParseResult.setStartTime(dateList.get(0));
        fileParseResult.setEndTime(dateList.get(dateList.size() - 1));
        fileParseResult.setDuration(ChronoUnit.DAYS.
                between(LocalDate.parse(dateList.get(0), formatter),
                        LocalDate.parse(dateList.get(dateList.size() - 1), formatter)) + "天");
    }
}
