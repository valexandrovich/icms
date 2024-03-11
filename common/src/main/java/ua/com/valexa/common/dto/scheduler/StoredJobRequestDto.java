package ua.com.valexa.common.dto.scheduler;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StoredJobRequestDto {
    private Long storedJobId;
    private String initiatorName;
    private Map<String, String> parameters = new HashMap<>();

    @Override
    public String toString() {
        return "StoredJobRequestDto{" +
                "storedJobId=" + storedJobId +
                ", initiatorName='" + initiatorName + '\'' +
                '}';
    }
}
