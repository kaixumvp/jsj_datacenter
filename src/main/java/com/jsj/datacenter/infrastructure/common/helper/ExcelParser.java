package com.jsj.datacenter.infrastructure.common.helper;

import com.jsj.datacenter.adapter.annotation.ExcelColumn;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/5/27
 */
public class ExcelParser<T> extends  FileParser<T> {

    public List<T> parse(File file, Class<T> clazz) throws Exception {
        // 创建Workbook

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<Integer, Field> columnMapping = buildColumnMapping(sheet, clazz);
            return parseSheet(sheet, clazz, columnMapping);
        }
    }

    private Map<Integer, Field> buildColumnMapping(Sheet sheet, Class<T> clazz) {
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();

        // 读取表头
        for (Cell cell : headerRow) {
            String headerName = cell.getStringCellValue().trim();
            headerMap.put(headerName, cell.getColumnIndex());
        }

        // 构建字段映射
        Map<Integer, Field> columnMapping = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                Integer columnIndex = headerMap.get(annotation.name());
                if (columnIndex != null) {
                    columnMapping.put(columnIndex, field);
                } else if (annotation.required()) {
                    throw new RuntimeException("缺少必需列: " + annotation.name());
                }
            }
        }
        return columnMapping;
    }

    private List<T> parseSheet(Sheet sheet, Class<T> clazz, Map<Integer, Field> columnMapping)
            throws Exception {

        List<T> result = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 从第2行开始
            Row row = sheet.getRow(i);
            if (row == null) continue;

            T instance = clazz.getDeclaredConstructor().newInstance();
            boolean isAllNull = true;
            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                Field field = columnMapping.get(columnIndex);
                if (field != null) {
                    setFieldValue(instance, field, cell);
                    isAllNull = false;
                }
            }
            if (isAllNull) {
                continue;
            }
            result.add(instance);
        }
        return result;
    }

    private void setFieldValue(Object obj, Field field, Cell cell) throws Exception {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        Object value = null;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    value = convertStringValue(cell.getStringCellValue(), fieldType, field);
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = convertDateValue(cell.getDateCellValue(), field);
                    } else {
                        value = convertNumericValue(cell.getNumericCellValue(), fieldType, field);
                    }
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                default:
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "第%d行第%d列数据转换失败: %s",
                    cell.getRowIndex() + 1,
                    cell.getColumnIndex() + 1,
                    e.getMessage()
            ));
        }

        if (value != null) {
            field.set(obj, value);
        }
    }

    private Object convertStringValue(String value, Class<?> targetType, Field field)
            throws Exception {

        if (value.isEmpty()) return null;

        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
        if (LocalDate.class.isAssignableFrom(targetType)) {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern(annotation.dateFormat()));
        }
        // 添加其他类型转换逻辑...
        return value;
    }

    private Object convertDateValue(Date date, Field field) throws Exception {
        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
        Class<?> targetType = field.getType();

        if (LocalDate.class.isAssignableFrom(targetType)) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (LocalDateTime.class.isAssignableFrom(targetType)) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return new SimpleDateFormat(annotation.dateFormat()).format(date);
    }

    // 修改convertNumericValue方法
    private Object convertNumericValue(double numericValue, Class<?> targetType, Field field) {
        // 新增对String类型的处理
        if (targetType == String.class) {
            return formatNumericToString(numericValue, field);
        }

        // 原有数值类型转换逻辑
        if (targetType == int.class || targetType == Integer.class) {
            return (int) numericValue;
        }
        if (targetType == long.class || targetType == Long.class) {
            return (long) numericValue;
        }
        if (targetType == double.class || targetType == Double.class) {
            return numericValue;
        }
        if (targetType == BigDecimal.class) {
            return BigDecimal.valueOf(numericValue);
        }
        return numericValue;
    }

    // 数值转字符串的格式化方法
    private String formatNumericToString(double value, Field field) {
        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

        // 模式1：智能去除".0"后缀（默认）
        if (value % 1 == 0) {
            return String.valueOf((long) value);
        }

        // 模式2：使用DecimalFormat格式化（需扩展注解）
        if (annotation != null && !annotation.numberFormat().isEmpty()) {
            return new DecimalFormat(annotation.numberFormat()).format(value);
        }

        // 默认转为完整字符串
        return String.valueOf(value);
    }
}