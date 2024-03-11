package ua.com.valexa.downloader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class QueueListener {

    @Autowired
    DownloaderService downloaderService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("queueSchedulerResponseName")
    String queueSchedulerResponseName;

    @RabbitListener(queues = "#{@queueDownloaderName}", errorHandler = "queueListenerErrorHandler")
    public void receiveDownloaderMessage(StepRequestDto stepRequestDto) {
        log.info("Downloader received message: " + stepRequestDto);
        CompletableFuture<StepResponseDto> cfuture = downloaderService.handleRequest(stepRequestDto);
        cfuture.thenAcceptAsync(this::sendResponse);
    }

    private void sendResponse(StepResponseDto stepResponseDto) {
        log.info("Downloader sending response for scheduler: " + stepResponseDto);
        rabbitTemplate.convertAndSend(queueSchedulerResponseName, stepResponseDto);
    }
}
