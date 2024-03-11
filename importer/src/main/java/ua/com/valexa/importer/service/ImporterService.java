package ua.com.valexa.importer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;
import ua.com.valexa.common.enums.TaskStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ImporterService {


    @Autowired
    @Qualifier("govua01")
    Importable importable;

    @Autowired
    ApplicationContext applicationContext;

    private final Map<String, AtomicBoolean> activeTasks = new ConcurrentHashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    protected CompletableFuture<StepResponseDto> handleRequest(StepRequestDto stepRequestDto) throws IllegalStateException {
        String workerName = stepRequestDto.getWorkerName();
        AtomicBoolean isRunning = activeTasks.computeIfAbsent(workerName, k -> new AtomicBoolean(false));

        if (!isRunning.compareAndSet(false, true)) {
            StepResponseDto stepResponseDto = new StepResponseDto();
            stepResponseDto.setStepId(stepRequestDto.getStepId());
            stepResponseDto.setComment(workerName + " вже виконує роботу");
            stepResponseDto.setStatus(TaskStatus.FAILED);
            return CompletableFuture.supplyAsync(() -> stepResponseDto, executorService);
        } else {
            importable = applicationContext.getBean(stepRequestDto.getWorkerName(), Importable.class);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return importable.handleRequest(stepRequestDto);
                } finally {
                    isRunning.set(false);
                }
            });
        }
    }

}
