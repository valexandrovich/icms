package ua.com.valexa.common.dto.scheduler;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StepRequestDto {
    private Long jobId;
    private Long stepId;
    private String workerName;
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public String toString() {
        return "StepRequestDto{" +
                "jobId=" + jobId +
                ", stepId=" + stepId +
                ", workerName='" + workerName + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
