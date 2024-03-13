package ua.com.valexa.webbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.web.LegalEntitySearchDto;
import ua.com.valexa.common.dto.web.PersonSearchDto;
import ua.com.valexa.db.service.red.*;
import ua.com.valexa.webbackend.model.SearchRedLeResult;
import ua.com.valexa.webbackend.model.SearchRedPpResult;

@Service
public class SearchRedPpService {

    @Autowired
    Govua01Service govua01Service;

    @Autowired
    Govua07Service govua07Service;

    @Autowired
    Govua08Service govua08Service;

    @Autowired
    Govua09Service govua09Service;

    @Autowired
    Govua10Service govua10Service;

    @Autowired
    Govua11Service govua11Service;

    @Autowired
    Govua12Service govua12Service;

    @Autowired
    Govua13Service govua13Service;


    public SearchRedPpResult searchPp(PersonSearchDto dto){

        SearchRedPpResult result = new SearchRedPpResult();
        result.setPersonSearchDto(dto);


        if (dto.getInn() != null && !dto.getInn().isBlank()){
            result.getGovua01List().addAll(govua01Service.findByCodeLike(dto.getInn()));

            result.getGovua07List().addAll(govua07Service.findByCodeLike(dto.getInn()));

        }

        if (dto.getLastName() != null && !dto.getLastName().isBlank()){
            StringBuilder fName = new StringBuilder();
            fName.append(dto.getLastName() == null ? "" : dto.getLastName());
            fName.append(dto.getFirstName() == null ? "" : " " + dto.getFirstName());
            fName.append(dto.getPatronymicName() == null ? "" : " " +  dto.getPatronymicName());

            result.getGovua01List().addAll(govua01Service.findByNameLike(fName.toString()));

            result.getGovua07List().addAll(govua07Service.findByNameLike(fName.toString()));

            result.getGovua08List().addAll(govua08Service.findByNameLike(dto.getLastName(), dto.getFirstName(), dto.getPatronymicName()));
            result.getGovua09List().addAll(govua09Service.findByNameLike(dto.getLastName(), dto.getFirstName(), dto.getPatronymicName()));
        }

        if (dto.getLocalPassportNumber() != null && !dto.getLocalPassportNumber().isBlank()){
            result.getGovua10List().addAll(govua10Service.findByPassportNumber(dto.getLocalPassportSerial(), dto.getLocalPassportNumber()));
            result.getGovua12List().addAll(govua12Service.findByPassportNumber(dto.getLocalPassportSerial(), dto.getLocalPassportNumber()));
        }

        if (dto.getIdPassportNumber() != null && !dto.getIdPassportNumber().isBlank()){
            result.getGovua10List().addAll(govua10Service.findByPassportNumber("", dto.getIdPassportNumber()));
            result.getGovua12List().addAll(govua12Service.findByPassportNumber("", dto.getIdPassportNumber()));
        }

        if (dto.getIntPassportNumber() != null && !dto.getIntPassportNumber().isBlank()){
            result.getGovua11List().addAll(govua11Service.findByPassportNumber(dto.getIntPassportSerial(), dto.getIntPassportNumber()));
            result.getGovua13List().addAll(govua13Service.findByPassportNumber(dto.getIntPassportSerial(), dto.getIntPassportNumber()));
        }


        return result;


    }





    public SearchRedLeResult searchLe(LegalEntitySearchDto dto){
        SearchRedLeResult result = new SearchRedLeResult();
        result.setLegalEntitySearchDto(dto);

        if (dto.getEdrpou() != null && !dto.getEdrpou().isBlank()){
            result.getGovua01List().addAll(govua01Service.findByCodeLike(dto.getEdrpou()));
            result.getGovua07List().addAll(govua07Service.findByCodeLike(dto.getEdrpou()));
        }

        return result;
    }


}
