package ua.com.valexa.importer.batch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileUrlResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ua.com.valexa.common.dto.red.GovUa07Dto;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;
import ua.com.valexa.common.dto.scheduler.TaskExecutionContextDto;
import ua.com.valexa.common.enums.TaskStatus;
import ua.com.valexa.db.model.red.GovUa07;
import ua.com.valexa.importer.mapper.GovUa07Mapper;

import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
//@EnableBatchProcessing(tablePrefix = "SYS.IMPORTER_BATCH")
@Slf4j
public class GovUa07Job {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    DataSource dataSource;

    @Autowired
    private ItemReader<GovUa07Dto> govUa07reader;

    @Autowired
    private ItemProcessor<GovUa07Dto, GovUa07> govUa07processor;

    @Autowired
    private JdbcBatchItemWriter<GovUa07> govUa07writer;

    @Autowired
    public ItemWriteListener<GovUa07> govUa07writerListener;


    private final int skipLines = 1;

    private int totalRowsCount;

    private long handledRowsCount;
    private long duplicateRowsCount;
    private long errorsCount;


    // Unizping file steps

    public Tasklet unzipFile() {
        return (contribution, chunkContext) -> {


            Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
            String filePathStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("file");
            File file = new File(filePathStr);

            TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
            taskExecutionContextDto.setStepId(stepId);
            taskExecutionContextDto.setComment("Розпаковка файла: " + filePathStr);
            taskExecutionContextDto.setProgress(0.0);
            taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);
            sendStepUpdate(taskExecutionContextDto);


            String csvFullPath = filePathStr.replaceAll("\\.zip$", ".csv");

            String extractedFilePath = null;

            try (ZipFile zipFile = new ZipFile(file, "CP1251")) {
                Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    File newFile = new File(csvFullPath);

                    if (entry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        try (InputStream inputStream = zipFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(newFile)) {
                            IOUtils.copy(inputStream, fos);
                        }
                        extractedFilePath = newFile.getAbsolutePath();
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                throw e;
            }

            taskExecutionContextDto.setComment("Файл розпаковано");
            sendStepUpdate(taskExecutionContextDto);

            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("extractedFile", extractedFilePath);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public TaskletStep govUa07unzipStep() {
        return new StepBuilder("govUa07unzipStep", jobRepository)
                .tasklet(unzipFile(), transactionManager)
                .build();
    }



    // Read and Import data step


    @Bean
    @StepScope
    public FlatFileItemReader<GovUa07Dto> govUa07reader(
            @Value("#{jobExecutionContext['extractedFile']}") String file
    ) {
        try {
            return new FlatFileItemReaderBuilder<GovUa07Dto>()
                    .resource(new FileUrlResource(file))
                    .name("govUa07reader")
                    .encoding("cp1251")
                    .delimited()
                    .delimiter(",")
                    .names("debtor_name", "debtor_birthdate", "debtor_code", "publisher", "org_name", "org_phone_num", "emp_full_fio", "emp_phone_num", "email_addr", "vp_ordernum", "vd_cat")
                    .targetType(GovUa07Dto.class)
                    .linesToSkip(skipLines)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @StepScope
    public ItemProcessor<GovUa07Dto, GovUa07> govUa07processor(
            @Value("#{jobParameters['stepId']}") String stepId,
            @Value("#{jobParameters['jobId']}") String jobId
    ) {
        return new ItemProcessor<GovUa07Dto, GovUa07>() {

            @Autowired
            GovUa07Mapper mapper;

            @Override
            public GovUa07 process(GovUa07Dto item) throws Exception {
                GovUa07 result = mapper.mapToEntity(item);
                result.setCreateRevisionId(Long.valueOf(jobId));
                result.setUpdateRevisionId(Long.valueOf(jobId));
                return result;
            }
        };
    }


    @Bean
    @StepScope
    public JdbcBatchItemWriter<GovUa07> govUa07writer(@Value("#{jobParameters['jobId']}") String jobId) {
        log.debug("write");
        log.debug("JOB ID: " + jobId);
        return new JdbcBatchItemWriterBuilder<GovUa07>()
                .sql("insert into red.govua_07 (" +
                        "hash, create_date, update_date,  create_revision_id, update_revision_id,  debtor_name, debtor_birthdate, debtor_code, publisher, org_name, org_phone_num, emp_full_fio, emp_phone_num, email_addr, vp_ordernum, vd_cat) " +
                        "VALUES (:hash, :createDate, :updateDate,  :createRevisionId, :updateRevisionId,  :debtorName, :debtorBirthdate, :debtorCode, :publisher, :orgName, :orgPhoneNum, :empFullFio, :empPhoneNum, :emailAddr, :vpOrdernum, :vdCat) " +
                        " ON CONFLICT (hash) DO UPDATE SET update_date = now(), update_revision_id = :createRevisionId")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .dataSource(dataSource)
                .assertUpdates(false)
                .build();
    }


    @Bean
    @StepScope
    public ItemWriteListener<GovUa07> govUa07writerListener(@Value("#{jobParameters['stepId']}") String stepId) {
        return new ItemWriteListener<>() {
            @Override
            public void afterWrite(Chunk<? extends GovUa07> items) {
                handledRowsCount += items.size();

                long divident = handledRowsCount  + errorsCount;
                double progress = (double)  divident / totalRowsCount;
//                System.out.println("afterWrite // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
                String comment = "Всього записів - " + totalRowsCount + "; Оброблено - " + handledRowsCount + "; Помилок - " +errorsCount+ ";";

                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(Long.valueOf(stepId));
                taskExecutionContextDto.setComment(comment);
                taskExecutionContextDto.setProgress(progress);
                sendStepUpdate(taskExecutionContextDto);

            }

            @Override
            public void beforeWrite(Chunk<? extends GovUa07> items) {
//                System.out.println("beforeWrite // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
            }

            @Override
            public void onWriteError(Exception exception, Chunk<? extends GovUa07> items) {
                if (exception instanceof DuplicateKeyException) {
//                    duplicateRowsCount++;

//                    System.out.println("onWriteError // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
                }
            }
        };
    }

    public TaskletStep govUa07importStep() {
        return new StepBuilder("govUa07importStep", jobRepository)
                .<GovUa07Dto, GovUa07>chunk(1000, transactionManager)
                .reader(govUa07reader)
                .processor(govUa07processor)
                .writer(govUa07writer)
                .listener(govUa07writerListener)
                .listener(govua07importStepExecutionListener())
                .faultTolerant()
                .skipPolicy(govuUa07skipPolicy())
                .build();
    }

    @Bean
    StepExecutionListener govua07importStepExecutionListener(){
        return new StepExecutionListener() {
            @Override
//            @Transactional
            public void beforeStep(StepExecution stepExecution) {

                String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString("extractedFile");
//                @Value("#{jobExecutionContext['extractedFile']}") String file


//                duplicateRowsCount = 0;
//                handledRowsCount = 0;
//                errorsCount = 0;
//                totalRowsCount = 0;
                Long stepId = Long.valueOf(stepExecution.getJobExecution().getJobParameters().getString("stepId"));
                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(Long.valueOf(stepId));
                taskExecutionContextDto.setComment("Аналіз файлу");
//                taskExecutionContextDto.setProgress(progress);
                sendStepUpdate(taskExecutionContextDto);

                File inputFile = new File(inputFilePath);
                try (BufferedReader br = new BufferedReader(new FileReader(inputFile))){
                    while (br.readLine() != null){
                        totalRowsCount++;
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                totalRowsCount = totalRowsCount - skipLines;

//                System.out.println("TOTAL ROWS : " + totalRowsCount);

//                etlStep = etlStepRepository.findById(etlSubtaskId).orElseThrow(() -> new EtlTaskNotFoundException(""));
//                etlStep.setStatus(EtlTaskStatus.IN_PROCESS);
//                etlStep.setDescription("Імпорт даних");
//                etlStep =  etlStepRepository.save(etlStep);
//                etlStepRepository.flush();
            }

            @Override
//            @Transactional
            public ExitStatus afterStep(StepExecution stepExecution) {
//                System.out.println("AFTER JOB  --  OK: " + handledRowsCount + "  DUP: " + duplicateRowsCount + " ERRORS:" + errorsCount);
//                etlStep.setComment("Опрацьовано: " + handledRowsCount + "  Дублікатів: " + duplicateRowsCount + " Помилок: " + errorsCount);
//                etlStep.setStatus(EtlStatus.COMPLETED);
//                etlStep.setProgress(1.0);
//                etlStep.setFinishedAt(LocalDateTime.now());
//                rabbitTemplate.convertAndSend(etlLoggerQueue, etlStep);
//                rabbitTemplate.convertAndSend("afg-etl-logger", etlStep);

//                etlStep.setDescription("Записано: " + handledRowsCount + " Дублікати: " + duplicateRowsCount + " Помилки: " + errorsCount);
//                etlStep.setProgress(Double.valueOf((handledRowsCount+duplicateRowsCount)) / totalRowsCount * 100);
//                etlStep.setFinishedAt(LocalDateTime.now());
//                etlStep.setStatus(EtlTaskStatus.FINISHED);
//                etlStep = etlStepRepository.save(etlStep);
//                etlStepRepository.flush();
                return ExitStatus.COMPLETED;
            }
        };
    }



    @Bean
    public Step govUa07newCountStep() {
        return new StepBuilder("govUa07newCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));

                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_07 where create_revision_id = :jobId";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    Integer count = namedParameterJdbcTemplate.queryForObject(sql, paramMap, Integer.class);

//                    System.out.println("NEW ROWS: " + count);
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("newCount", count);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


    @Bean
    public Step govUa07updateCountStep() {
        return new StepBuilder("govUa07updateCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));

                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_07 where update_revision_id = :jobId and create_revision_id <> :jobId";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    Integer count = namedParameterJdbcTemplate.queryForObject(sql, paramMap, Integer.class);

//                    System.out.println("UPDATE ROWS: " + count);
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("updateCount", count);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step govUa07disableCountStep() {
        return new StepBuilder("govUa07disableCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));


                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_07 where disable_revision_id = :jobId";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    Integer count = namedParameterJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
//                    System.out.println("DISABLED ROWS: " + count);
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("disableCount", count);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


    @Bean
    @StepScope
    public SkipPolicy govuUa07skipPolicy() {
        return (t, skipCount) -> {

            if (!(t instanceof DuplicateKeyException)) {
                errorsCount++;
//                System.out.println("onWriteError // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
//                log.error("Non duplicate error " + t.getMessage());

            }
            return true;
        };
    }


    // Disabling tags step

    @Bean
    public Step govUa07disablingStep() {
        return new StepBuilder("govUa07disabledStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));
                    String sql = "UPDATE red.govua_07 SET disable_date = now(), disable_revision_id = :jobId where (create_revision_id <> :jobId or create_revision_id is null) and (update_revision_id<> :jobId or update_revision_id is null) and disable_revision_id is null;";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }





//    @Bean
//    @StepScope
//    public SkipPolicy govuUa07skipPolicy() {
//        return (t, skipCount) -> {
//
//            if (!(t instanceof DuplicateKeyException)) {
//                log.error("Non duplicate error ");
//            }
//            return true;
//        };
//    }


    @Bean("govUa07job")
    public Job govUa07job(JobRepository jobRepository) {
        return new JobBuilder("govUa07job", jobRepository)
                .start(govUa07unzipStep())
                .next(govUa07importStep())
                .next(govUa07disablingStep())

                .next(govUa07newCountStep())
                .next(govUa07updateCountStep())
                .next(govUa07disableCountStep())

                .listener(govUa07jobExecutionListener())
                .build();
    }

    @Bean
    public JobExecutionListener govUa07jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                handledRowsCount = 0;
                errorsCount = 0;
                totalRowsCount = 0;
//                System.out.println("beforeJob // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
                Long stepId = Long.valueOf(jobExecution.getJobParameters().getString("stepId"));
                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(stepId);
                taskExecutionContextDto.setComment("Початок імпорту");
                taskExecutionContextDto.setProgress(0.0);
                taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);
                sendStepUpdate(taskExecutionContextDto);

//                System.out.println("beforeJob // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
//                Long stepId = Long.valueOf(jobExecution.getJobParameters().getString("stepId"));
//                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
//                taskExecutionContextDto.setStepId(stepId);
//                taskExecutionContextDto.setComment("Початок імпорту");
//                taskExecutionContextDto.setProgress(0.0);
//                taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);
//                sendStepUpdate(taskExecutionContextDto);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
//                System.out.println("afterJob // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(Long.valueOf(jobExecution.getJobParameters().getString("stepId")));
                String comment =
                        "Імпорт завершено. Нових записів - " + jobExecution.getExecutionContext().get("newCount")
                                + "; Дублікатів - " + jobExecution.getExecutionContext().get("updateCount")
                                + "; Деактивованих записів - " + jobExecution.getExecutionContext().get("disableCount") + ";";

                taskExecutionContextDto.setComment(comment);
                sendStepUpdate(taskExecutionContextDto);
            }
        };
    }


    private void sendStepUpdate(TaskExecutionContextDto taskExecutionContextDto){
        StepUpdateDto stepUpdateDto = new StepUpdateDto();
        stepUpdateDto.setStepId(taskExecutionContextDto.getStepId());
        stepUpdateDto.setStatus(taskExecutionContextDto.getStatus());
        stepUpdateDto.setComment(taskExecutionContextDto.getComment());
        stepUpdateDto.setProgress(taskExecutionContextDto.getProgress());
        rabbitTemplate.convertAndSend("icms.cpms", stepUpdateDto);
    }
}
