package ua.com.valexa.importer.batch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ua.com.valexa.common.dto.red.GovUa13Dto;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;
import ua.com.valexa.common.dto.scheduler.TaskExecutionContextDto;
import ua.com.valexa.common.enums.TaskStatus;
import ua.com.valexa.db.model.red.GovUa13;
import ua.com.valexa.importer.mapper.GovUa13Mapper;
import ua.com.valexa.importer.service.JsonStreamingItemReader;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class GovUa13Job {

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
    private ItemReader<GovUa13Dto> govua13reader;

    @Autowired
    private ItemProcessor<GovUa13Dto, GovUa13> govua13processor;

    @Autowired
    private JdbcBatchItemWriter<GovUa13> govua13writer;

    @Autowired
    public ItemWriteListener<GovUa13> govua13writerListener;


    private final int skipLines = 1;

    private int totalRowsCount;

    private long handledRowsCount;
//    private long duplicateRowsCount;
    private long errorsCount;


    // Unizping file steps


    // Read and Import data step


    @Bean
    @StepScope
    public JsonStreamingItemReader<GovUa13Dto> govua13Reader(
            @Value("#{jobParameters['file']}") String filePath
    ) throws Exception {
        return new JsonStreamingItemReader<>(filePath, GovUa13Dto.class);
    }

    @Bean
    @StepScope
    public ItemProcessor<GovUa13Dto, GovUa13> govua13processor(
            @Value("#{jobParameters['stepId']}") String stepId,
            @Value("#{jobParameters['jobId']}") String jobId
    ) {
        return new ItemProcessor<>() {

            @Autowired
            GovUa13Mapper mapper;

            @Override
            public GovUa13 process(GovUa13Dto item) {
                GovUa13 result = mapper.mapToEntity(item);
                result.setCreateRevisionId(Long.valueOf(jobId));
                result.setUpdateRevisionId(Long.valueOf(jobId));
                return result;
            }
        };
    }


    @Bean
    @StepScope
    public JdbcBatchItemWriter<GovUa13> govua13writer(@Value("#{jobParameters['jobId']}") String jobId) {
        return new JdbcBatchItemWriterBuilder<GovUa13>()
                .sql("insert into red.govua_13 (" +
                        "hash, create_date, update_date,  create_revision_id, update_revision_id, id, ovd, series, number, type, status, theft_date, insert_date) " +
                        "VALUES (:hash, :createDate, :updateDate,  :createRevisionId, :updateRevisionId,  :id, :ovd, :series, :number, :type, :status, :theftDate, :insertDate) " +
                        " ON CONFLICT (hash) DO UPDATE SET update_date = now(), update_revision_id = :createRevisionId")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .dataSource(dataSource)
                .assertUpdates(false)
                .build();
    }


    @Bean
    @StepScope
    public ItemWriteListener<GovUa13> govua13writerListener(
            @Value("#{jobParameters['stepId']}") String stepId)
    {
        return new ItemWriteListener<>() {
            @Override
            public void afterWrite(Chunk<? extends GovUa13> items) {
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
            public void beforeWrite(Chunk<? extends GovUa13> items) {
                System.out.print("");




            }

            @Override
            public void onWriteError(Exception exception, Chunk<? extends GovUa13> items) {
                if (exception instanceof DuplicateKeyException) {
//                    duplicateRowsCount++;
//                    System.out.println("onWriteError // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
                } else {

                }
            }
        };
    }

    public TaskletStep govua13importStep() {
        return new StepBuilder("govua13importStep", jobRepository)
                .<GovUa13Dto, GovUa13>chunk(1000, transactionManager)
                .reader(govua13reader)
                .processor(govua13processor)
                .writer(govua13writer)
                .listener(govua13writerListener)
                .listener(govua13importStepExecutionListener())
                .faultTolerant()
                .skipPolicy(govuUa13skipPolicy())
                .build();
    }

    @Bean
    StepExecutionListener govua13importStepExecutionListener(){
        return new StepExecutionListener() {
            @Override
//            @Transactional
            public void beforeStep(StepExecution stepExecution) {

                String inputFilePath = stepExecution.getJobExecution().getJobParameters().getString("file");
                Long stepId = Long.valueOf(stepExecution.getJobExecution().getJobParameters().getString("stepId"));
                TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(Long.valueOf(stepId));
                taskExecutionContextDto.setComment("Аналіз файлу");
//                taskExecutionContextDto.setProgress(progress);
                sendStepUpdate(taskExecutionContextDto);
//                duplicateRowsCount = 0;
//                handledRowsCount = 0;
//                errorsCount = 0;
//                totalRowsCount = 0;
                File inputFile = new File(inputFilePath);

                int totalObjectsCount = 0;

                try (JsonParser jsonParser = new JsonFactory().createParser(inputFile)) {
                    // Check if the first token is the start of an array
                    if (jsonParser.nextToken() == JsonToken.START_ARRAY) {
                        // Iterate over all tokens in the array
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            // Move to the start of the next object in the array
                            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                                totalRowsCount++;
                                // Optionally, you can process the object here if needed
                                // For simply counting, we skip to the end of the current object
                                jsonParser.skipChildren();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found: " + inputFilePath, e);
                } catch (IOException e) {
                    throw new RuntimeException("Error reading JSON file: " + inputFilePath, e);
                }


//                try (BufferedReader br = new BufferedReader(new FileReader(inputFile))){
//                    while (br.readLine() != null){
//                        totalRowsCount++;
////                        System.out.println("TRC: " + totalRowsCount);
//                    }
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                totalRowsCount = totalRowsCount - skipLines;

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
//                etlStep.setProgress(Double.valueOf((handledRowsCount+duplicateRowsCount)) / totalRowsCount * 080);
//                etlStep.setFinishedAt(LocalDateTime.now());
//                etlStep.setStatus(EtlTaskStatus.FINISHED);
//                etlStep = etlStepRepository.save(etlStep);
//                etlStepRepository.flush();
                return ExitStatus.COMPLETED;
            }
        };
    }


//    @Bean
//    public Step govua08duplicateCountStep() {
//        return new StepBuilder("govua08duplicateCountStep", jobRepository)
//                .tasklet((contribution, chunkContext) -> {
//                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));
//                    String sql = "select count(*) from red.govua_01 where update_revision_id = :jobId and create_revision_id <> :jobId";
//                    Map<String, Object> paramMap = new HashMap<>();
//                    paramMap.put("jobId", jobId);
//                    Integer count = namedParameterJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
//                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("duplicateCount", count);
//                    return RepeatStatus.FINISHED;
//                }, transactionManager)
//                .build();
//    }

    @Bean
    public Step govua13newCountStep() {
        return new StepBuilder("govua13newCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));

                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_13 where create_revision_id = :jobId";
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
    public Step govua13updateCountStep() {
        return new StepBuilder("govua13updateCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));


                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_13 where update_revision_id = :jobId and create_revision_id <> :jobId";
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
    public Step govua13disableCountStep() {
        return new StepBuilder("govua13disableCountStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));

                    Long stepId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("stepId"));
                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                    taskExecutionContextDto.setStepId(stepId);
                    taskExecutionContextDto.setComment("Аналіз нових записів");
                    sendStepUpdate(taskExecutionContextDto);


                    String sql = "select count(*) from red.govua_13 where disable_revision_id = :jobId";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    Integer count = namedParameterJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
//                    System.out.println("DISABLED ROWS: " + count);
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("disableCount", count);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


    // Disabling tags step

    @Bean
    public Step govua13disablingStep() {
        return new StepBuilder("govua13disabledStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Long jobId = Long.valueOf(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString("jobId"));
                    String sql = "UPDATE red.govua_13 SET disable_date = now(), disable_revision_id = :jobId where (create_revision_id <> :jobId or create_revision_id is null) and (update_revision_id<> :jobId or update_revision_id is null) and disable_revision_id is null;";
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("jobId", jobId);
                    int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();


    }


    @Bean
    @StepScope
    public SkipPolicy govuUa13skipPolicy() {
        return (t, skipCount) -> {

            if (!(t instanceof DuplicateKeyException)) {
                errorsCount++;
//                System.out.println("onWriteError // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);
//                log.error("Non duplicate error " + t.getMessage());

            }
            return true;
        };
    }


    @Bean("govUa13job")
    public Job govUa13job(JobRepository jobRepository) {
        return new JobBuilder("govUa13job", jobRepository)
//                .start(govua08unzipStep())
                .start(govua13importStep())
                .next(govua13disablingStep())
                .next(govua13newCountStep())
                .next(govua13updateCountStep())
                .next(govua13disableCountStep())
                .listener(govua13jobExecutionListener())
                .build();
    }

    @Bean
    public JobExecutionListener govua13jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
//                duplicateRowsCount = 0;
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
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
//                System.out.println("afterJob // handledRowsCount: " + handledRowsCount + "  duplicateRowsCount: " + duplicateRowsCount + " errorsCount:" + errorsCount);



                    TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
                taskExecutionContextDto.setStepId(Long.valueOf(jobExecution.getJobParameters().getString("stepId")));

                if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)){
                    String comment =
                            "Імпорт завершено. Нових записів - " + jobExecution.getExecutionContext().get("newCount")
                                    + "; Дублікатів - " + jobExecution.getExecutionContext().get("updateCount")
                                    + "; Деактивованих записів - " + jobExecution.getExecutionContext().get("disableCount") + ";";

                    taskExecutionContextDto.setComment(comment);
                } else {
                    taskExecutionContextDto.setComment(jobExecution.getExitStatus().getExitDescription());

                }
                sendStepUpdate(taskExecutionContextDto);




//                System.out.println("NEW COUNT: " + jobExecution.getExecutionContext().get("newCount"));
//                System.out.println("UPDATE COUNT: " + jobExecution.getExecutionContext().get("updateCount"));
//                System.out.println("DISABLE COUNT: " + jobExecution.getExecutionContext().get("disableCount"));


            }
        };
    }


    private void sendStepUpdate(TaskExecutionContextDto taskExecutionContextDto) {
        StepUpdateDto stepUpdateDto = new StepUpdateDto();
        stepUpdateDto.setStepId(taskExecutionContextDto.getStepId());
        stepUpdateDto.setStatus(taskExecutionContextDto.getStatus());
        stepUpdateDto.setComment(taskExecutionContextDto.getComment());
        stepUpdateDto.setProgress(taskExecutionContextDto.getProgress());
        rabbitTemplate.convertAndSend("icms.cpms", stepUpdateDto);
    }
}
