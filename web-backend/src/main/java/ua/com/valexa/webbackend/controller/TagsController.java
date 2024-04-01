package ua.com.valexa.webbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.valexa.db.model.data.tag.TagType;
import ua.com.valexa.db.repository.data.tag.TagTypeRepository;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagsController
{
    @Autowired
    TagTypeRepository tagTypeRepository;

    @GetMapping("/all")
    public List<TagType> getAllTagTypes(){
        return tagTypeRepository.findAll();
    }
}
