package ua.com.valexa.scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;
import ua.com.valexa.common.dto.scheduler.StoredJobRequestDto;
import ua.com.valexa.common.enums.TaskStatus;
import ua.com.valexa.db.model.sys.Job;
import ua.com.valexa.db.model.sys.Step;
import ua.com.valexa.db.model.sys.StoredJob;
import ua.com.valexa.db.model.sys.StoredStep;
import ua.com.valexa.db.repository.sys.JobRepository;
import ua.com.valexa.db.repository.sys.StepRepository;
import ua.com.valexa.db.repository.sys.StoredJobRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class SchedulerService {

    @Autowired
    StoredJobRepository storedJobRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    StepRepository stepRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Autowired
    @Qualifier("queueCpmsName")
    String queueCpmsName;


    @Autowired
    @Qualifier("queueDownloaderName")
    String queueDownloaderName;

    @Autowired
    @Qualifier("queueImporterName")
    String queueImporterName;


    private final Map<Long, AtomicBoolean> activeStoredJobs = new ConcurrentHashMap<>();


    public void initStoredJob(StoredJobRequestDto storedJobRequestDto) {


        AtomicBoolean isRunning = activeStoredJobs.computeIfAbsent(storedJobRequestDto.getStoredJobId(), k -> new AtomicBoolean(false));
        StoredJob storedJob = storedJobRepository.findById(storedJobRequestDto.getStoredJobId()).orElseThrow(() -> new RuntimeException("Can't find Stored Job with id: " + storedJobRequestDto.getStoredJobId()));
        log.debug("Stored Job id: " + storedJob.getId() + " is locked");
        if (!isRunning.compareAndSet(false, true)) {
            Job job = new Job();
            job.setStoredJob(storedJob);
            job.setStartedAt(LocalDateTime.now());
            job.setInitiatorName(storedJobRequestDto.getInitiatorName());
//            job.setStatus(TaskStatus.SKIPPED);
            job = jobRepository.save(job);
        } else {
            log.debug("Initing Stored Job: " + storedJobRequestDto.getStoredJobId());
            try {


                Job job = new Job();
                job.setStoredJob(storedJob);
                job.setStartedAt(LocalDateTime.now());
                job.setInitiatorName(storedJobRequestDto.getInitiatorName());
                job = jobRepository.save(job);

                log.debug("Created new Job id: " + job.getId());

                StoredStep firstStep = getFirstStep(storedJob);

                Step step = new Step();
                step.setJob(job);
                step.setStatus(TaskStatus.NEW);
                step.setStartedAt(LocalDateTime.now());
                step.setStoredStep(firstStep);
                step = stepRepository.save(step);

                log.debug("Created new Step id: " + step.getId());


                StepRequestDto stepRequestDto = new StepRequestDto();
                stepRequestDto.setJobId(job.getId());
                stepRequestDto.setStepId(step.getId());
                stepRequestDto.setWorkerName(firstStep.getWorkerName());
                stepRequestDto.setParameters(firstStep.getParameters());
                stepRequestDto.getParameters().put("sourceNickName", storedJob.getSourceNickName());
                stepRequestDto.getParameters().putAll(storedJobRequestDto.getParameters());

                sendStepRequest(stepRequestDto, firstStep.getServiceName());

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

    }


    public void initNextStep(StepResponseDto stepResponseDto) {
        try {
            Step previousStep = stepRepository.findById(stepResponseDto.getStepId()).orElseThrow(() -> new RuntimeException("Can't find step with id:" + stepResponseDto.getStepId()));
            previousStep.setFinishedAt(LocalDateTime.now());
            previousStep = stepRepository.save(previousStep);
            sendStepUpdate(stepResponseDto.getStepId(), stepResponseDto.getStatus(), stepResponseDto.getComment(), null);

            StoredStep nextSroredStep = findNextStep(previousStep.getStoredStep().getStepOrder(), previousStep.getStoredStep().getStoredJob());
            Job job = previousStep.getJob();
            job.getResults().putAll(stepResponseDto.getResults());
            job = jobRepository.save(job);


            if (nextSroredStep == null) {
                finishJob(stepResponseDto);
            } else {

                Step step = new Step();
                step.setJob(previousStep.getJob());
                step.setStatus(TaskStatus.NEW);
                step.setStartedAt(LocalDateTime.now());
                step.setStoredStep(nextSroredStep);
                step = stepRepository.save(step);

                log.debug("Created new Step id: " + step.getId());

                StepRequestDto stepRequestDto = new StepRequestDto();
                stepRequestDto.setJobId(previousStep.getJob().getId());
                stepRequestDto.setStepId(step.getId());
                stepRequestDto.setWorkerName(nextSroredStep.getWorkerName());
                stepRequestDto.setParameters(nextSroredStep.getParameters());
                stepRequestDto.getParameters().put("sourceNickName", nextSroredStep.getStoredJob().getSourceNickName());
                stepRequestDto.getParameters().putAll(job.getResults());
                sendStepRequest(stepRequestDto, nextSroredStep.getServiceName());

            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private StoredStep getFirstStep(StoredJob storedJob) throws RuntimeException {
        Optional<StoredStep> firstStep = storedJob.getSteps().stream()
                .filter(StoredStep::getIsEnabled)
                .min(Comparator.comparingInt(StoredStep::getStepOrder));
        if (firstStep.isPresent()) {
            return firstStep.get();
        } else {
            throw new RuntimeException("Cant find first enabled step in Stored Job: " + storedJob.getId());
        }
    }

    private StoredStep findNextStep(Integer previousStepOrder, StoredJob storedJob) {
        return storedJob.getSteps().stream()
                .filter(storedStep -> storedStep.getStepOrder() > previousStepOrder && storedStep.getIsEnabled())
                .min(Comparator.comparingInt(StoredStep::getStepOrder))
                .orElse(null);
    }

    private void finishJob(StepResponseDto stepResponseDto) {
        log.info("Trying to finishing job: " + stepResponseDto);
        Step previousStep = stepRepository.findById(stepResponseDto.getStepId()).orElseThrow(() -> new RuntimeException("Can't find step with id:" + stepResponseDto.getStepId()));
        Job currentJob = previousStep.getJob();
        currentJob.setFinishedAt(LocalDateTime.now());
//        if (stepResponseDto.getStatus().equals(TaskStatus.FINISHED) || (stepResponseDto.getStatus().equals(TaskStatus.FAILED) && previousStep.getStoredStep().getIsSkippable())) {
////            currentJob.setStatus(TaskStatus.FINISHED);
//        } else {
////            currentJob.setStatus(TaskStatus.FAILED);
//
//        }
        jobRepository.save(currentJob);
        activeStoredJobs.computeIfPresent(currentJob.getStoredJob().getId(), (k, v) -> {
            v.set(false);
            return v;
        });
        log.debug("Stored Job id: " + currentJob.getStoredJob().getId() + " is unlocked");
    }

    private void sendStepRequest(StepRequestDto stepRequestDto, String serviceName) {

        switch (serviceName) {
            case "downloader": {
                log.info("Sending request to DOWNLOADER : " + stepRequestDto);
                rabbitTemplate.convertAndSend(queueDownloaderName, stepRequestDto);
                break;
            }
            case "importer": {
                log.info("Sending request to IMPORTER : " + stepRequestDto);
                rabbitTemplate.convertAndSend(queueImporterName, stepRequestDto);
                break;
            }
//            case "transformer": {
//                log.info("Sending request to TRANSFORMER : " + stepRequestDto);
//                rabbitTemplate.convertAndSend("icams.transformer", stepRequestDto);
//                break;
//            }
//            case "enricher": {
//                log.info("Sending request to ENRICHER : " + stepRequestDto);
//                rabbitTemplate.convertAndSend("icams.enricher", stepRequestDto);
//                break;
//            }
        }

    }

    private void sendStepUpdate(Long stepId, TaskStatus status, String comment, Double progress) {
        StepUpdateDto stepUpdateDto = new StepUpdateDto();
        stepUpdateDto.setStepId(stepId);
        stepUpdateDto.setStatus(status);
        stepUpdateDto.setComment(comment);
        stepUpdateDto.setProgress(progress);
        rabbitTemplate.convertAndSend(queueCpmsName, stepUpdateDto);
    }

}
