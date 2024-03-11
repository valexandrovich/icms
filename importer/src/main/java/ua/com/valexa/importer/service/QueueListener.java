package ua.com.valexa.importer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    ImporterService importerService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("queueSchedulerResponseName")
    String queueSchedulerResponseName;

    @RabbitListener(queues = "#{@queueImporterName}", errorHandler = "queueListenerErrorHandler")
    public void receiveDownloaderMessage(StepRequestDto stepRequestDto) {
            log.info("Importer received message: " + stepRequestDto);
            CompletableFuture<StepResponseDto> cfuture = importerService.handleRequest(stepRequestDto);
            cfuture.thenAcceptAsync(this::sendResponse);

    }

    private void sendResponse(StepResponseDto stepResponseDto) {
        log.info("Importer sending response for scheduler: " + stepResponseDto);
        rabbitTemplate.convertAndSend(queueSchedulerResponseName, stepResponseDto);
    }
}
