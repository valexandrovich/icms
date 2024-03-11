package ua.com.valexa.cpms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;
import ua.com.valexa.db.model.sys.Step;
import ua.com.valexa.db.repository.sys.StepRepository;

import java.util.Optional;

@Service
public class CpmsService {

    @Autowired
    StepRepository stepRepository;

    public void updateStep(StepUpdateDto stepUpdateDto) {
        System.out.println("Updating step " + stepUpdateDto);
        Optional<Step> stepOptional = stepRepository.findById(stepUpdateDto.getStepId());
        if (stepOptional.isPresent()) {
            Step step = stepOptional.get();
            if (stepUpdateDto.getComment() != null) {
                step.setComment(stepUpdateDto.getComment());
            }
            if (stepUpdateDto.getProgress() != null) {
                step.setProgress(stepUpdateDto.getProgress());
            }
            if (stepUpdateDto.getStatus() != null) {
                step.setStatus(stepUpdateDto.getStatus());
            }
            stepRepository.save(step);
        }
    }


}
