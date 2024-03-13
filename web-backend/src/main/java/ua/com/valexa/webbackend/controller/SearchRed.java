package ua.com.valexa.webbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.valexa.common.dto.web.LegalEntitySearchDto;
import ua.com.valexa.common.dto.web.PersonSearchDto;
import ua.com.valexa.webbackend.model.SearchRedLeResult;
import ua.com.valexa.webbackend.model.SearchRedPpResult;
import ua.com.valexa.webbackend.service.SearchRedPpService;

@RestController
@RequestMapping("/search-red")
public class SearchRed {

    @Autowired
    SearchRedPpService searchRedPpService;

    @PostMapping("/pp")
    public SearchRedPpResult searchPP(@RequestBody PersonSearchDto dto){
        return searchRedPpService.searchPp(dto);
    }

    @PostMapping("/le")
    public SearchRedLeResult searchLe(@RequestBody LegalEntitySearchDto dto){
        return searchRedPpService.searchLe(dto);
    }

}
