package ua.com.valexa.common.dto.web;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonSearchDto {
    String lastName;
    String firstName;
    String patronymicName;
    LocalDate birthday;
    String inn;

    private String localPassportSerial;
    private String localPassportNumber;


    private String intPassportSerial;
    private String intPassportNumber;

    private String idPassportNumber;
}
