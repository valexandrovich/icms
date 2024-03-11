package ua.com.valexa.common.dto.scheduler;

import lombok.Data;
import ua.com.valexa.common.enums.TaskStatus;

import java.util.HashMap;
import java.util.Map;

@Data
public class StepResponseDto {
    private Long stepId;
    private TaskStatus status;
    private Map<String, String> results = new HashMap<>();
    private String comment;

    @Override
    public String toString() {
        return "StepResponseDto{" +
                "stepId=" + stepId +
                ", status=" + status +
                ", results=" + results +
                ", comment='" + comment + '\'' +
                '}';
    }
}
