package com.jsj.datacenter.adapter.dto.river;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jsj.datacenter.infrastructure.vo.CalculateViewVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RiverCalculateResult {
    private String resultExcelUrl;
    private List discrepancy;
    private CalculateViewVO views;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    private String calculateResultFileKey;  //计算结果文件key
    private String calculateResultFileName;
    private String comparisonDataFileKey;   //计算对比文件key
    private String comparisonDataFileName;
    private String differenceFileKey;       //计算误差结果文件key
    private String differenceFileName;


    private List<String> differenceResultImage;
    private List<String> calculateResultImage;
}
