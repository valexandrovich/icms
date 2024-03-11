package ua.com.valexa.cpms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;

@Service
@Slf4j
public class QueueListener {


    @Autowired
    CpmsService cpmsService;

    @Autowired
    @Qualifier("queueCpmsName")
    String queueCpmsName;


    @RabbitListener(queues = "#{@queueCpmsName}", errorHandler = "queueListenerErrorHandler")
    public void receiveDownloaderMessage(StepUpdateDto stepUpdateDto) {
        log.info("CPMS received message: " + stepUpdateDto);
        cpmsService.updateStep(stepUpdateDto);
    }

}