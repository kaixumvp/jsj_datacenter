package com.jsj.datacenter.adapter.dto.envmeasures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jsj.datacenter.infrastructure.common.enums.EvnMeasureType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.*;

/**
 * @author 罗林欣
 * @version 0.0.1
 * @date 2025/6/5
 */
@Data
public class EnvMeasuresListDTO {

    @Schema(description = "所有事件")
    public Map<EvnMeasureType, Event> events;

    public void init() {
        if (null == events) {
            events = new HashMap<>();
        }
    }

    public void addMeasure(Measure measure) {
        if (null == measure) {
            return;
        }
        Event event = events.get(measure.getEventType());
        if (null == event) {
            event = new Event();
            event.setMeasures(new ArrayList<>());
            event.setMeasuresMap(new HashMap<>());
            event.setTotalCount(0);
            event.setDoneCount(0);
            event.setDoingCount(0);
            event.setTodoCount(0);
        }
        event.getMeasuresMap().put(measure.getTitle(), measure);
        // 转成list并排序
        event.setMeasures(new ArrayList<>(event.getMeasuresMap().values()));
        event.getMeasures().sort(Comparator.comparingInt(Measure::getWeigh));
        event.setTotalCount(event.getTotalCount() + measure.getDoneCount() + measure.getDoingCount() + measure.getTodoCount());
        event.setDoneCount(event.getDoneCount() + measure.getDoneCount());
        event.setDoingCount(event.getDoingCount() + measure.getDoingCount());
        event.setTodoCount(event.getTodoCount() + measure.getTodoCount());
        events.put(measure.getEventType(), event);
    }

    @Data
    public static class Event {
        @JsonIgnore
        private Map<String, Measure> measuresMap;
        @Schema(description = "措施内容")
        private List<Measure> measures;
        @Schema(description = "总任务数")
        private Integer totalCount;
        @Schema(description = "已完成数")
        private Integer doneCount;
        @Schema(description = "正在实施数量")
        private Integer doingCount;
        @Schema(description = "计划实施数量")
        private Integer todoCount;
    }

    @Data
    public static class Measure {
        @JsonIgnore
        private EvnMeasureType eventType;
        @JsonIgnore
        private Integer weigh;
        @Schema(description = "主要措施")
        private String title;
        @Schema(description = "措施详细信息")
        private List<EnvMeasuresDTO> items;
        @Schema(description = "已完成数")
        private Integer doneCount;
        @Schema(description = "正在实施数量")
        private Integer doingCount;
        @Schema(description = "计划实施数量")
        private Integer todoCount;

        public void init() {
            if (null == items) {
                items = new ArrayList<>();
            }
            if (null == doneCount) {
                doneCount = 0;
            }
            if (null == doingCount) {
                doingCount = 0;
            }
            if (null == todoCount) {
                todoCount = 0;
            }
        }

        public void addItem(EnvMeasuresDTO item) {
            if (null == item) {
                return;
            }
            this.init();
            items.add(item);
            // items中根据weigh排序
            items.sort(Comparator.comparingInt(EnvMeasuresDTO::getWeigh));
            title = items.get(0).getTitle();
            weigh = items.get(0).getWeigh();
            eventType = item.getEventType();
            if (item.getImplCondition() == 1) {
                doneCount++;
            } else if (item.getImplCondition() == 2) {
                doingCount++;
            } else {
                todoCount++;
            }
        }
    }
}
