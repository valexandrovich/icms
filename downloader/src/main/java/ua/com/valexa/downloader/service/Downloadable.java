package ua.com.valexa.downloader.service;

import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;

public interface Downloadable {
    StepResponseDto handleRequest(StepRequestDto stepRequestDto);
}
