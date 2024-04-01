package ua.com.valexa.webbackend.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ua.com.valexa.common.dto.scheduler.StoredJobRequestDto;
import ua.com.valexa.common.dto.web.UploadPpRequestDto;
import ua.com.valexa.common.dto.web.UploadPpRowDto;
import ua.com.valexa.db.model.red.UploaderPpRow;
import ua.com.valexa.db.model.sys.UploaderRevisionPP;
import ua.com.valexa.db.repository.stage.UploaderFilePpRepository;
import ua.com.valexa.db.repository.stage.UploaderPpRowRepository;
import ua.com.valexa.webbackend.mapper.UploaderPPRowMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private String schedulerInitQueue = "icms.scheduler-stored-job-init";

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Autowired
    UploaderPpRowRepository uploaderPpRowRepository;

    @Autowired
    UploaderFilePpRepository uploaderFilePpRepository;

    @Autowired
    UploaderPPRowMapper uploaderPPRowMapper;

    @GetMapping("/pp/revisions")
    public List<UploaderRevisionPP> getRevisions(){
        return uploaderFilePpRepository.findAll();
    }

    @PostMapping("/pp/init/{revisionId}")
    public void initPpRevision(@PathVariable UUID revisionId){
        System.out.println(revisionId);

        StoredJobRequestDto storedJobRequestDto = new StoredJobRequestDto();
        storedJobRequestDto.setStoredJobId(2L);
        storedJobRequestDto.getParameters().put("revisionId", revisionId.toString());
        storedJobRequestDto.setInitiatorName("valex");

        rabbitTemplate.convertAndSend(schedulerInitQueue, storedJobRequestDto);
    }

    @PostMapping("/pp")
    public void loadPpData(
            @RequestBody UploadPpRequestDto requestDto
    ) {
//        System.out.println(requestDto);

        List<UploaderPpRow> rows = new ArrayList<>();
        for (UploadPpRowDto dto : requestDto.getData()){
            rows.add(uploaderPPRowMapper.mapToEntity(dto));
        }
        uploaderPpRowRepository.saveAll(rows);


//        UploaderRevisionPP ufp = new UploaderRevisionPP();
//        ufp.setRevisionName(requestDto.getRevisionName());
//        ufp.setId(UUID.randomUUID());
//        ufp.setCreateDate(LocalDateTime.now());
//        ufp.setInitiatorName(requestDto.getInitiatorName());
//        ufp.setRowsCount(requestDto.getData().size());
//        ufp = uploaderFilePpRepository.save(ufp);
//
//        List<UploaderPpRow> rows = new ArrayList<>();
//        for (UploadPpRowDto rowDto : requestDto.getData()) {
//            UploaderPpRow row = uploaderPPRowMapper.mapToEntity(rowDto);
//            row.setUploaderFile(ufp);
//            rows.add(row);
//        }
//        uploaderPpRowRepository.saveAll(rows);

    }
}
