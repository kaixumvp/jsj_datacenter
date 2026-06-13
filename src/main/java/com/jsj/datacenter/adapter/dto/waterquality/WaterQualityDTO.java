package com.jsj.datacenter.adapter.dto.waterquality;

import com.jsj.datacenter.infrastructure.vo.WaterQualityItemVO;
import lombok.Data;

import java.util.List;

@Data
public class WaterQualityDTO {
    /**
     * 水质数据周期
     */
    private String period;

    /**
     * 文件key
     */
    private String fileKey;

    /**
     * 水质数据
     */
    private List<WaterQualityItemVO> data;
}
