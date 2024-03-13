package ua.com.valexa.webbackend.model;

import lombok.Data;
import ua.com.valexa.common.dto.web.LegalEntitySearchDto;
import ua.com.valexa.common.dto.web.PersonSearchDto;
import ua.com.valexa.db.model.red.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchRedLeResult {
    LegalEntitySearchDto legalEntitySearchDto;
    List<GovUa01> govua01List = new ArrayList<>();
    List<GovUa07> govua07List = new ArrayList<>();
}
