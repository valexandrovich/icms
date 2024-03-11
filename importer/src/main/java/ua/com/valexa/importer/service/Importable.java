package ua.com.valexa.importer.service;


import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;

public interface Importable {
    StepResponseDto handleRequest(StepRequestDto stepRequestDto);
}
