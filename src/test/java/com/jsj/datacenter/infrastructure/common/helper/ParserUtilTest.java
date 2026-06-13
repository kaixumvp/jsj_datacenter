package com.jsj.datacenter.infrastructure.common.helper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserUtilTest {

    @Test
    void getParser_withExcel_returnsExcelParser() throws Exception {
        FileParser<?> parser = ParserUtil.getParser("excel");
        assertNotNull(parser);
        assertInstanceOf(ExcelParser.class, parser);
    }

    @Test
    void getParser_withUnknownType_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            ParserUtil.getParser("csv");
        });
        assertTrue(ex.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void getParser_withNull_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            ParserUtil.getParser(null);
        });
        assertTrue(ex.getMessage().contains("不支持的文件类型"));
    }
}
