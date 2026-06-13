package com.jsj.datacenter.infrastructure.common.entity;

import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表格分页数据对象
 * 
 * @author ruoyi
 */
@Data

public class TableDataInfo<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<T> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     * 分页
     * 
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<T> list, int total)
    {
        this.rows = list;
        this.total = total;
    }

    public static TableDataInfo nullReturn(){
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(HttpStatus.SC_OK);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(new ArrayList<>());
        dataInfo.setTotal(0);
        return dataInfo;
    }
    /**
     * 响应请求分页数据
     *
     * @param list 分页查询数据
     * @return TableDataInfo
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static  <T> TableDataInfo<T> getDataTable(List<T> list) {
        TableDataInfo<T> rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SC_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo<>(list).getTotal());
        return rspData;
    }

    /**
     * 响应请求分页数据
     *
     * @param list  分页查询数据
     * @param clazz 通用转换类，需要保证可实例化，有setter
     * @return TableDataInfo
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> TableDataInfo<T> getDataTable(List<?> list, Class<T> clazz) {
        TableDataInfo<T> rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SC_OK);
        rspData.setMsg("查询成功");
        // 将list中的数据转换为clazz类型的对象
        List<T> data = new ArrayList<>();
        for (Object o : list) {
            T t = BeanUtils.instantiateClass(clazz);
            BeanUtils.copyProperties(o, t);
            data.add(t);
        }
        rspData.setRows(data);
        rspData.setTotal(new PageInfo<>(list).getTotal());
        return rspData;
    }
}
