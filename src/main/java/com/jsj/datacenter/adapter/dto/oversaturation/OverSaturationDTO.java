package com.jsj.datacenter.adapter.dto.oversaturation;

import com.jsj.datacenter.infrastructure.vo.OverSaturationItemVO;
import com.jsj.datacenter.infrastructure.vo.PointPositionItemVO;
import lombok.Data;

import java.util.List;

@Data
public class OverSaturationDTO {
    /**
     * 周期
     */
    private String period;

    /**
     * 文件key
     */
    private String fileKey;

    /**
     * 多个点位数据
     */
    private List<PointPositionItemVO> pointPositions;

    /**
     * 过饱和数据
     */
    //private List<OverSaturationItemVO> data;
}
