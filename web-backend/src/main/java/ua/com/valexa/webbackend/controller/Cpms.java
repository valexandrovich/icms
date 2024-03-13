package ua.com.valexa.webbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.valexa.db.model.sys.Job;
import ua.com.valexa.db.repository.sys.JobRepository;

import java.util.List;

@RestController
@RequestMapping("/cpms")
public class Cpms {
    @Autowired
    JobRepository jobRepository;

    @GetMapping("/jobs")
    public List<Job> getAllJobs(){
        return jobRepository.findAll();
    }
}
