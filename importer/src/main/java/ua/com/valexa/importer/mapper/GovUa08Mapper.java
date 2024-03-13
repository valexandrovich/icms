package ua.com.valexa.importer.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;
import ua.com.valexa.common.dto.red.GovUa08Dto;
import ua.com.valexa.db.model.red.GovUa08;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

@Mapper(componentModel = "spring")
@Component
public interface GovUa08Mapper {
//    @Mapping(source="nn", target="nn")
//    @Mapping(source="status", target="status")
//    @Mapping(source="series", target="series")
//    @Mapping(source="number", target="number")
    @Mapping(expression = "java(parseDateFromString(dto.getLostDate()))", target = "lostDate"  )
    @Mapping(expression = "java(parseDateFromString(dto.getBirthday()))", target = "birthday"  )
//    @Mapping(source="dateEdit", target="dateEdit")
//    @Mapping(source="start_date_auc", target="startDateAuc")
//    @Mapping(source="end_date_auc", target="endDateAuc")
//    @Mapping(source="court_name", target="courtName")
//    @Mapping(source="end_registration_date", target="endRegistrationDate")
    GovUa08 mapToEntity(GovUa08Dto dto);


    @AfterMapping
    default void afterNapping(GovUa08Dto dto, @MappingTarget GovUa08 entity){
        entity.generateHash();
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
    }
    //
//


    default LocalDate parseDateFromString(String dateString) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .optionalEnd()
                .toFormatter();

        try {
            TemporalAccessor temporalAccessor = formatter.parseBest(dateString, LocalDateTime::from, LocalDate::from);
            if (temporalAccessor instanceof LocalDateTime) {
                return ((LocalDateTime) temporalAccessor).toLocalDate();
            } else if (temporalAccessor instanceof LocalDate) {
                return (LocalDate) temporalAccessor;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }
        return null;
    }


//    static LocalDate parseDateFromString(String dateString) {
//        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
//                .appendPattern("d/M/yyyy HH:mm:ss")
//                .optionalStart()
//                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
//                .optionalEnd()
//                .toFormatter();
//
//        try {
//            TemporalAccessor temporalAccessor = formatter.parseBest(dateString, LocalDateTime::from, LocalDate::from);
//            if (temporalAccessor instanceof LocalDateTime) {
//                return ((LocalDateTime) temporalAccessor).toLocalDate();
//            } else if (temporalAccessor instanceof LocalDate) {
//                return (LocalDate) temporalAccessor;
//            }
//        } catch (DateTimeParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private static String normalizeDateTimeString(String dateTimeStr, int fractionalDigits) {
//        int dotIndex = dateTimeStr.indexOf('.');
//        if (dotIndex != -1 && dateTimeStr.length() - dotIndex - 1 > fractionalDigits) {
//            // Truncate the string to the desired number of fractional digits
//            return dateTimeStr.substring(0, dotIndex + fractionalDigits + 1);
//        }
//        return dateTimeStr; // Return the original string if no truncation is needed
//    }
}
