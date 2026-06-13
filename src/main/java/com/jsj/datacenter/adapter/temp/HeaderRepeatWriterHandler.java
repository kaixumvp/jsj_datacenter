package com.jsj.datacenter.adapter.temp;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class HeaderRepeatWriterHandler implements RowWriteHandler, SheetWriteHandler {
    private final Iterator<Map.Entry<Integer, String>> iterator;
    private Map.Entry<Integer, String> next;

    public HeaderRepeatWriterHandler(TreeMap<Integer, String> headList) {
        this.iterator = headList.entrySet().iterator();
        next = this.iterator.next();
    }


    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {
        Sheet sheet = writeSheetHolder.getSheet();
        if (row.getRowNum() == next.getKey()) {
            sheet.shiftRows(next.getKey(), sheet.getLastRowNum(), 1);
            Row row1 = sheet.createRow(next.getKey());
            row1.createCell(0).setCellValue(next.getValue());
            writeSheetHolder.setRelativeHeadRowIndex(relativeRowIndex+1);
            if (iterator.hasNext()) {
                next = iterator.next();
            }
        }
    }

}
