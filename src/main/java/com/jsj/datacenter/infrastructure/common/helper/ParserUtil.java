package com.jsj.datacenter.infrastructure.common.helper;

public class ParserUtil {
    public static <T> FileParser<T> getParser(String fileType) throws Exception {
        switch (fileType) {
            case "excel":
                return new ExcelParser<>();
            default:
                throw new Exception("不支持的文件类型");
        }
    }
}
