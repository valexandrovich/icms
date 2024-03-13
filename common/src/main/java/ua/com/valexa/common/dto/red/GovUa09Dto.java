package ua.com.valexa.common.dto.red;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GovUa09Dto {
        @JsonProperty("ID")
        private String id;
        @JsonProperty("OVD")
        private String ovd;
        @JsonProperty("CATEGORY")
        private String category;
        @JsonProperty("FIRST_NAME_U")
        private String firstNameUa;
        @JsonProperty("LAST_NAME_U")
        private String lastNameUa;
        @JsonProperty("MIDDLE_NAME_U")
        private String patronymicNameUa;

        @JsonProperty("FIRST_NAME_R")
        private String firstNameRu;
        @JsonProperty("LAST_NAME_R")
        private String lastNameRu;
        @JsonProperty("MIDDLE_NAME_R")
        private String patronymicNameRu;

        @JsonProperty("FIRST_NAME_E")
        private String firstNameEn;
        @JsonProperty("LAST_NAME_E")
        private String lastNameEn;
        @JsonProperty("MIDDLE_NAME_E")
        private String patronymicNameEn;


        @JsonProperty("BIRTH_DATE")
        private String birthday;
        @JsonProperty("SEX")
        private String sex;
        @JsonProperty("LOST_DATE")
        private String lostDate;
        @JsonProperty("LOST_PLACE")
        private String lostPlace;
        @JsonProperty("ARTICLE_CRIM")
        private String articleCrim;
        @JsonProperty("RESTRAINT")
        private String restraint;
        @JsonProperty("CONTACT")
        private String contact;
        @JsonProperty("PHOTOID")
        private String photoid;

}
