package com.jsj.datacenter.infrastructure.vo;

import lombok.Data;

import java.util.List;

@Data
public class CalculateViewVO {

    /**********************************
     * 水库模型结果图片返回
     *********************************/
    //下泄水温图
    private String xxswUrl;
    //二维云图动画
    private String gifUrl;
    //坝前垂向水温图
    private String bqcxswUrl;
    //二维温度变化云图
    private String cxwdUrl;
    //下泄水温差异
    private String xxswDiffUrl;
    //垂向水温
    private String cxwdDiffUrl;

    /**********************************
     * 河道模型结果图片返回
     *********************************/
    //沿程水温
    private String ycswGifUrl;
    //断面水温
    private String dmswUrl;
    //差异水温沿程变化
    private List<String> ycswDiffUrl;
    //差异断面水温变化
    private String dmswDiffUrl;
}
