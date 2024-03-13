package ua.com.valexa.webbackend.model;

import lombok.Data;
import ua.com.valexa.common.dto.web.PersonSearchDto;
import ua.com.valexa.db.model.red.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchRedPpResult {
    PersonSearchDto personSearchDto;
    List<GovUa01> govua01List = new ArrayList<>();
    List<GovUa07> govua07List = new ArrayList<>();
    List<GovUa08> govua08List = new ArrayList<>();
    List<GovUa09> govua09List = new ArrayList<>();
    List<GovUa10> govua10List = new ArrayList<>();
    List<GovUa11> govua11List = new ArrayList<>();
    List<GovUa12> govua12List = new ArrayList<>();
    List<GovUa13> govua13List = new ArrayList<>();
}
