package ua.com.valexa.importer.service.govua;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;
import ua.com.valexa.common.dto.scheduler.TaskExecutionContextDto;
import ua.com.valexa.common.enums.TaskStatus;
import ua.com.valexa.importer.service.Importable;

import java.util.Date;
import java.util.Map;

@Service("govua11")
@Slf4j
public class GovUa11Importer implements Importable {


    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("govUa11job")
    Job job;

    @Override
    public StepResponseDto handleRequest(StepRequestDto stepRequestDto) {

        TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
        taskExecutionContextDto.setStepId(stepRequestDto.getStepId());
        taskExecutionContextDto.setProgress(0.0);
        taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);

        StepResponseDto stepResponseDto = new StepResponseDto();
        stepResponseDto.setStepId(stepRequestDto.getStepId());

        try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addString("startedAt", new Date().toString());
            jobParametersBuilder.addString("stepId", String.valueOf(stepRequestDto.getStepId()));
            jobParametersBuilder.addString("jobId", String.valueOf(stepRequestDto.getJobId()));
            for (Map.Entry<String, String> entry : stepRequestDto.getParameters().entrySet()) {
                jobParametersBuilder.addString(entry.getKey(), entry.getValue());
            }
            JobParameters jp = jobParametersBuilder.toJobParameters();
            JobExecution jobExecution =  jobLauncher.run(job, jp);

            if (jobExecution.getStatus().equals(BatchStatus.FAILED)){
                stepResponseDto.setComment(jobExecution.getExitStatus().getExitDescription());
                stepResponseDto.setStatus(TaskStatus.FAILED);
                return stepResponseDto;
            }
        } catch (Exception e) {
            log.error("Importer failed : " + e.getMessage());
            stepResponseDto.setComment(e.getMessage());
            stepResponseDto.setStatus(TaskStatus.FAILED);
        }
        stepResponseDto.setStatus(TaskStatus.FINISHED);
        return stepResponseDto;


//        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
//
//        jobParametersBuilder.addString("startedAt", new Date().toString());
//        jobParametersBuilder.addString("stepId", String.valueOf(stepRequestDto.getStepId()));
//        jobParametersBuilder.addString("jobId", String.valueOf(stepRequestDto.getJobId()));
//
//        for (Map.Entry<String, String> entry : stepRequestDto.getParameters().entrySet()){
//            jobParametersBuilder.addString(entry.getKey(), entry.getValue());
//        }
//        JobParameters jp = jobParametersBuilder.toJobParameters();
//        try {
//            JobExecution jobExecution =  jobLauncher.run(job, jp);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//
//
//        System.out.println(stepRequestDto);
//        return new StepResponseDto();
    }
}
