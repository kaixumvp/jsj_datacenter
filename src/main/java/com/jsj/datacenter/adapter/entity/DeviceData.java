package com.jsj.datacenter.adapter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * DeviceData
 *
 * @description: 设备数据实体类
 * @author: ${USER}
 * @date: ${DATE} ${TIME}
 * @version: 1.0
 */
@Data
@TableName("device_data")
public class DeviceData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long clientid;

    private String deviceNo;

    private Long timestamp;

    private Double nox;

    private Double co;

    private Double hcl;

    private Double so2;

    private Double dust;

    private Date createTime;

    private Date updateTime;
}
