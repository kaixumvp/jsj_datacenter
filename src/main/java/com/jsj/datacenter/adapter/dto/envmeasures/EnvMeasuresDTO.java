package com.jsj.datacenter.adapter.dto.envmeasures;

import com.jsj.datacenter.infrastructure.common.enums.EvnMeasureType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Data
public class EnvMeasuresDTO {
    @Schema(description = "id，新增措施时不用传")
    private Long id;

    @Schema(description = "事件类型", example = "ENV_APPROVAL")
    @NotNull(message = "事件类型不能为空")
    private EvnMeasureType eventType;

    @Schema(description = "主要措施", example = "严格落实水环境保护措施")
    @NotEmpty(message = "主要措施不能为空")
    private String title;

    @Schema(description = "排序")
    @Range(min = 1, max = 100, message = "排序值范围为1-100")
    private Integer weigh;

    @Schema(description = "内容")
    @NotEmpty(message = "内容不能为空")
    private String content;

    @Schema(description = "落实情况, 1:已完成, 2:正在实施, 3:计划实施", example = "1", allowableValues = {"1", "2", "3"})
    @NotNull(message = "落实情况不能为空")
    @Range(min = 1, max = 3, message = "落实情况值异常")
    private Integer implCondition;

    @Schema(description = "当前进展")
    private String process;
}
