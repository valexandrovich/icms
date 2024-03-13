package ua.com.valexa.webbackend.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ua.com.valexa.common.dto.scheduler.StoredJobRequestDto;
import ua.com.valexa.db.model.sys.StoredJob;
import ua.com.valexa.db.repository.sys.StoredJobRepository;

import java.util.List;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    StoredJobRepository storedJobRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${QUEUE_SCHEDULER_STORED_JOB_INIT_NAME}")
    private String queueSchedulerStoredJobInitName;

    @GetMapping("/stored-jobs")
    public List<StoredJob> getAllStoredJobs(){
        return storedJobRepository.findAll();
    }

    @PostMapping("/init")
    public void initStoredJob(@RequestBody StoredJobRequestDto dto){
        rabbitTemplate.convertAndSend(queueSchedulerStoredJobInitName, dto);
    }

}
