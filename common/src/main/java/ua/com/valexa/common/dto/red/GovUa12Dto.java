package ua.com.valexa.common.dto.red;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GovUa12Dto {
        @JsonProperty("ID")
        private String id;
        @JsonProperty("OVD")
        private String ovd;
        @JsonProperty("CATEGORY")
        private String category;
        @JsonProperty("D_SERIES")
        private String series;
        @JsonProperty("D_NUMBER")
        private String number;
        @JsonProperty("D_TYPE")
        private String type;
        @JsonProperty("D_STATUS")
        private String status;
        @JsonProperty("THEFT_DATA")
        private String theftDate;
        @JsonProperty("INSERT_DATE")
        private String insertDate;

}
