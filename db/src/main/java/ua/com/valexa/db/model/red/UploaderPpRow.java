package ua.com.valexa.db.model.red;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import ua.com.valexa.common.enums.Country;
import ua.com.valexa.common.enums.Sex;
import ua.com.valexa.db.model.sys.UploaderRevisionPP;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "red", name = "uploader_pp")
@Data
public class UploaderPpRow {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "uploader_file_id")
    private UploaderRevisionPP uploaderFile;

    @Column(name="last_name_ua")
    private String lastNameUa;

    @Column(name="first_name_ua")
    private String firstNameUa;
    @Column(name="patronymic_name_ua")
    private String patronymicNameUa;
    @Column(name="last_name_ru")
    private String lastNameRu;
    @Column(name="first_name_ru")
    private String firstNameRu;
    @Column(name="patronymic_name_ru")
    private String patronymicNameRu;
    @Column(name="last_name_en")
    private String lastNameEn;
    @Column(name="first_name_en")
    private String firstNameEn;
    @Column(name="patronymic_name_en")
    private String patronymicNameEn;
    @Column(name="birthday")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate birthday;
    @Column(name="inn")
    private String inn;
//    @Column(name="tax_code")
//    private String taxCode;
    @Column(name="local_pass_serial")
    private String localPassportSerial;
    @Column(name="local_pass_number")
    private String localPassportNumber;
    @Column(name="local_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate localPassportIssueDate;
    @Column(name="local_pass_issuer_name")
    private String localPassportIssuerName;
    @Column(name="id_pass_number")
    private String idPassportNumber;
    @Column(name="id_pass_record")
    private String idPassportRecord;
    @Column(name="id_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate idPassportIssueDate;
    @Column(name="id_pass_issuer_code")
    private String idPassportIssuerCode;
    @Column(name="int_pass_serial")
    private String intPassportSerial;
    @Column(name="int_pass_number")
    private String intPassportNumber;
    @Column(name="int_pass_issue_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate intPassportIssueDate;
    @Column(name="int_pass_issuer_code")
    private String intPassportIssuer;
    @Column(name="citizenship")
    private Country citizenship;
    @Column(name="birthplace")
    private String birthplace;
    @Column(name="sex")
    private Sex sex;
    @Column(name="phone")
    private String phone;
    @Column(name="email")
    private String email;
    @Column(name="address_simple")
    private String addressSimple;
    @Column(name="address_country")
    private String addressCountry;
    @Column(name="address_region")
    private String addressRegion;
    @Column(name="address_county")
    private String addressCounty;
    @Column(name="address_city_type")
    private String addressCityType;
    @Column(name="address_city")
    private String addressCity;
    @Column(name="address_street_type")
    private String addressStreetType;
    @Column(name="address_street")
    private String addressStreet;
    @Column(name="address_building_number")
    private String addressBuildingNumber;
    @Column(name="address_building_letter")
    private String addressBuildingLetter;
    @Column(name="address_building_part")
    private String addressBuildingPart;
    @Column(name="address_apartment")
    private String addressApartment;
    @Column(name="mk_code")
    private String mkCode;
    @Column(name="mk_event_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkEventDate;
    @Column(name="mk_start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkStartDate;
    @Column(name="mk_end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate mkEndDate;
    @Column(name="mk_text_value")
    private String mkTextValue;
    @Column(name="mk_number_value")
    private Integer mkNumberValue;
    @Column(name="mk_comment")
    private String mkComment;
    @Column(name="source")
    private String source;
    
}
