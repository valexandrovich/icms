package ua.com.valexa.common.dto.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UploadPpRequestDto {

    @JsonProperty("initiatorName")
    private String initiatorName;
    @JsonProperty("revisionName")
    private String revisionName;
    @JsonProperty("data")
    private List<UploadPpRowDto> data;

}
