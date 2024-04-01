package ua.com.valexa.common.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ua.com.valexa.common.enums.Country;
import ua.com.valexa.common.enums.Sex;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UploadPpRowDto {

//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("last_name_ua")
    private String lastNameUa;

    @JsonProperty("first_name_ua")
    private String firstNameUa;
    @JsonProperty("patronymic_name_ua")
    private String patronymicNameUa;
    @JsonProperty("last_name_ru")
    private String lastNameRu;
    @JsonProperty("first_name_ru")
    private String firstNameRu;
    @JsonProperty("patronymic_name_ru")
    private String patronymicNameRu;
    @JsonProperty("last_name_en")
    private String lastNameEn;
    @JsonProperty("first_name_en")
    private String firstNameEn;
    @JsonProperty("patronymic_name_en")
    private String patronymicNameEn;
    @JsonProperty("birthday")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate birthday;
    @JsonProperty("inn")
    private String inn;
    @JsonProperty("local_pass_serial")
    private String localPassportSerial;
    @JsonProperty("local_pass_number")
    private String localPassportNumber;
    @JsonProperty("local_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate localPassportIssueDate;
    @JsonProperty("local_pass_issuer_name")
    private String localPassportIssuerName;
    @JsonProperty("id_pass_number")
    private String idPassportNumber;
    @JsonProperty("id_pass_record")
    private String idPassportRecord;
    @JsonProperty("id_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate idPassportIssueDate;
    @JsonProperty("id_pass_issuer_code")
    private String idPassportIssuerCode;
    @JsonProperty("int_pass_serial")
    private String intPassportSerial;
    @JsonProperty("int_pass_number")
    private String intPassportNumber;
    @JsonProperty("int_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate intPassportIssueDate;
    @JsonProperty("int_pass_issuer_code")
    private String intPassportIssuer;
    @JsonProperty("citizenship")
    private Country citizenship;
    @JsonProperty("birthplace")
    private String birthplace;
    @JsonProperty("sex")
    private Sex sex;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("address_simple")
    private String addressSimple;
    @JsonProperty("address_country")
    private String addressCountry;
    @JsonProperty("address_region")
    private String addressRegion;
    @JsonProperty("address_county")
    private String addressCounty;
    @JsonProperty("address_city_type")
    private String addressCityType;
    @JsonProperty("address_city")
    private String addressCity;
    @JsonProperty("address_street_type")
    private String addressStreetType;
    @JsonProperty("address_street")
    private String addressStreet;
    @JsonProperty("address_building_number")
    private String addressBuildingNumber;
    @JsonProperty("address_building_letter")
    private String addressBuildingLetter;
    @JsonProperty("address_building_part")
    private String addressBuildingPart;
    @JsonProperty("address_apartment")
    private String addressApartment;
    @JsonProperty("mk_code")
    private String mkCode;
    @JsonProperty("mk_event_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkEventDate;
    @JsonProperty("mk_start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkStartDate;
    @JsonProperty("mk_end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkEndDate;
    @JsonProperty("mk_text_value")
    private String mkTextValue;
    @JsonProperty("mk_number_value")
    private Integer mkNumberValue;
    @JsonProperty("mk_comment")
    private String mkComment;
    @JsonProperty("source")
    private String source;

}
