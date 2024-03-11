package ua.com.valexa.downloader.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloaderService {

    @Autowired
    @Qualifier("govua01")
    Downloadable downloadable;

    @Autowired
    ApplicationContext applicationContext;
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    protected CompletableFuture<StepResponseDto> handleRequest(StepRequestDto stepRequestDto) {
        downloadable = applicationContext.getBean(stepRequestDto.getWorkerName(), Downloadable.class);
        return CompletableFuture.supplyAsync(() -> downloadable.handleRequest(stepRequestDto), executorService);
    }

}
