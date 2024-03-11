package ua.com.valexa.common.dto.scheduler;

import lombok.Data;
import ua.com.valexa.common.enums.TaskStatus;

@Data
public class TaskExecutionContextDto {
    private Long stepId;
    private String comment;
    private Double progress;
    private TaskStatus status;
}
