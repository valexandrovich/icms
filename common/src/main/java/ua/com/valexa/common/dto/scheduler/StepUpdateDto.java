package ua.com.valexa.common.dto.scheduler;

import lombok.Data;
import ua.com.valexa.common.enums.TaskStatus;

@Data
public class StepUpdateDto {
    private Long stepId;
    private Double progress;
    private TaskStatus status;
    private String comment;
}
