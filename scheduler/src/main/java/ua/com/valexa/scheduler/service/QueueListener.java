package ua.com.valexa.scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;
import ua.com.valexa.common.dto.scheduler.StoredJobRequestDto;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    @Qualifier("queueSchedulerResponseName")
    String queueSchedulerResponseName;

    @Autowired
    @Qualifier("queueSchedulerStoredJobInitName")
    String queueSchedulerStoredJobInitName;

    @RabbitListener(queues = "#{@queueSchedulerStoredJobInitName}", errorHandler = "queueListenerErrorHandler")
    public void receiveDownloaderMessage(StoredJobRequestDto storedJobRequestDto){
        log.info("Scheduler receive init message: " + storedJobRequestDto);
        schedulerService.initStoredJob(storedJobRequestDto);
    }

    @RabbitListener(queues = "#{@queueSchedulerResponseName}", errorHandler = "queueListenerErrorHandler")
    public void receiveDownloaderMessage(StepResponseDto stepResponseDto){
        log.info("Scheduler receive response message: " + stepResponseDto);
        schedulerService.initNextStep(stepResponseDto);
    }

}
