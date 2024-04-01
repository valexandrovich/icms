package ua.com.valexa.webbackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ua.com.valexa.common.dto.web.UploadPpRowDto;
import ua.com.valexa.db.model.red.UploaderPpRow;

@Mapper(componentModel = "spring")
@Component
public interface UploaderPPRowMapper {
    @Mapping(source="uuid", target="id")
    @Mapping(source="lastNameUa", target="lastNameUa")
//    @Mapping(expression = "java(parseDateFromString(dto.getDate()))", target = "date"  )
//    @Mapping(source="type", target="type")
//    @Mapping(source="firm_edrpou", target="firmEdrpou")
//    @Mapping(source="firm_name", target="firmName")
//    @Mapping(source="case_number", target="caseNumber")
//    @Mapping(source="start_date_auc", target="startDateAuc")
//    @Mapping(source="end_date_auc", target="endDateAuc")
//    @Mapping(source="court_name", target="courtName")
//    @Mapping(source="end_registration_date", target="endRegistrationDate")
    UploaderPpRow mapToEntity(UploadPpRowDto dto);
}
