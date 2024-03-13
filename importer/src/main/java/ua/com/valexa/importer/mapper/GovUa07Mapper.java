package ua.com.valexa.importer.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;
import ua.com.valexa.common.dto.red.GovUa07Dto;
import ua.com.valexa.db.model.red.GovUa07;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
@Component
public interface GovUa07Mapper {
    @Mapping(source = "debtorName", target = "debtorName")
    @Mapping(expression = "java(parseDateFromString(dto.getDebtorBirthdate()))", target = "debtorBirthdate")
    @Mapping(source = "debtorCode", target = "debtorCode")
    @Mapping(source = "publisher", target = "publisher")
    @Mapping(source = "orgName", target = "orgName")
    @Mapping(source = "orgPhoneNum", target = "orgPhoneNum")
    @Mapping(source = "empFullFio", target = "empFullFio")
    @Mapping(source = "empPhoneNum", target = "empPhoneNum")
    @Mapping(source = "emailAddr", target = "emailAddr")
    @Mapping(source = "vpOrdernum", target = "vpOrdernum")
    @Mapping(source = "vdCat", target = "vdCat")
    GovUa07 mapToEntity(GovUa07Dto dto);

    @AfterMapping
    default void afterNapping(GovUa07Dto dto, @MappingTarget GovUa07 entity) {
        entity.generateHash();
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
    }

    default LocalDate parseDateFromString(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.M.yyyy");
        try {
            return LocalDate.ofInstant(simpleDateFormat.parse(dateString).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            return null;
        }
    }
}
