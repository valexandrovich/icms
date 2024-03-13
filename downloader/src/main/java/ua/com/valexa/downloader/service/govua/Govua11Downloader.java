package ua.com.valexa.downloader.service.govua;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.valexa.common.dto.scheduler.StepRequestDto;
import ua.com.valexa.common.dto.scheduler.StepResponseDto;
import ua.com.valexa.common.dto.scheduler.StepUpdateDto;
import ua.com.valexa.common.dto.scheduler.TaskExecutionContextDto;
import ua.com.valexa.common.enums.TaskStatus;
import ua.com.valexa.downloader.service.Downloadable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service("govua11")
@Slf4j
public class Govua11Downloader implements Downloadable {

    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${downloader.mount.point}")
    private String mountPoint;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public StepResponseDto handleRequest(StepRequestDto stepRequestDto) {


        TaskExecutionContextDto taskExecutionContextDto = new TaskExecutionContextDto();
        taskExecutionContextDto.setStepId(stepRequestDto.getStepId());
        taskExecutionContextDto.setProgress(0.0);
        taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);

        StepResponseDto stepResponseDto = new StepResponseDto();
        stepResponseDto.setStepId(stepRequestDto.getStepId());


        try {
            taskExecutionContextDto.setComment("Зчитування параметрів");
            sendStepUpdate(taskExecutionContextDto);
            StepParameters stepParameters = readParameters(stepRequestDto.getStepId(), stepRequestDto.getParameters());

            taskExecutionContextDto.setComment("Зчитування метадати пакету");
            sendStepUpdate(taskExecutionContextDto);
            JsonNode packageMetadata = getPackageMetadata(stepParameters);

            taskExecutionContextDto.setComment("Зчитування актуального ресурсу");
            sendStepUpdate(taskExecutionContextDto);
            String actualResourceId = getActualResourceId(packageMetadata, stepParameters);

            taskExecutionContextDto.setComment("Зчитування метадати ресурсу");
            sendStepUpdate(taskExecutionContextDto);
            GovuaRevisionMetadata metadata = getRevisionMetadata(actualResourceId, stepParameters);

            String fileName = mountPoint + System.getProperty("file.separator") + stepRequestDto.getJobId() + "_" + stepParameters.getSourceName() + "." + metadata.getFileExtension();

            String downloadedFile = downloadFile(metadata.getUrl(), fileName, stepParameters, taskExecutionContextDto);

            stepResponseDto.getResults().put("file", downloadedFile);
            stepResponseDto.setStatus(TaskStatus.FINISHED);
            stepResponseDto.setComment(taskExecutionContextDto.getComment());
        } catch (Exception e){
            log.error("Downloading failed : " + e.getMessage());
            stepResponseDto.setComment(e.getMessage());
            stepResponseDto.setStatus(TaskStatus.FAILED);
        }
        return stepResponseDto;
    }

    private JsonNode getPackageMetadata(StepParameters stepParameters) throws RuntimeException{
        try {
            int currentTry = 0;
            JsonNode jsonResponse = null;

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(stepParameters.requestTimeoutSec * 1000)
                    .setConnectionRequestTimeout(stepParameters.requestTimeoutSec * 1000)
                    .setSocketTimeout(stepParameters.requestTimeoutSec * 1000)
                    .build();

            CloseableHttpClient client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .build();

            while (currentTry < stepParameters.requestRetries) {
                try {
                    HttpGet request = new HttpGet("https://data.gov.ua/api/3/action/package_show?id=" + stepParameters.getPackageId());
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("Accept", "application/json");

                    HttpResponse response = client.execute(request);
                    String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    jsonResponse = objectMapper.readTree(responseStr);
                    break;
                } catch (IOException e) {
                    currentTry++;
                    if (currentTry >= stepParameters.requestRetries) {
                        throw e;
                    }
                }
            }
            return jsonResponse;
        } catch (IOException e){
            throw new RuntimeException("Can't get package metadata for " + stepParameters.getSourceName());
        }

    }
    private String getActualResourceId(JsonNode packageMetadata, StepParameters stepParameters) throws RuntimeException{
        try {
            JsonNode resources = packageMetadata.get("result").get("resources");
            return resources.get(resources.size() -1).get("id").textValue();
        } catch (Exception e){
            throw new RuntimeException("Can't get actual resource id for " + stepParameters.sourceName);
        }

    }
    private GovuaRevisionMetadata getRevisionMetadata(String resourceId, StepParameters stepParameters) throws RuntimeException {
        try {
            int currentTry = 0;
            JsonNode jsonResponse = null;

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(stepParameters.requestTimeoutSec * 1000)
                    .setConnectionRequestTimeout(stepParameters.requestTimeoutSec * 1000)
                    .setSocketTimeout(stepParameters.requestTimeoutSec * 1000)
                    .build();

            CloseableHttpClient client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .build();

            while (currentTry < stepParameters.requestRetries) {
                try {
                    HttpGet request = new HttpGet("https://data.gov.ua/api/3/action/resource_show?id=" + resourceId);
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("Accept", "application/json");

                    HttpResponse response = client.execute(request);
                    String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    jsonResponse = objectMapper.readTree(responseStr);
                    String urlStr = jsonResponse.get("result").get("url").textValue();

                    GovuaRevisionMetadata metadata = new GovuaRevisionMetadata();
                    metadata.setUrl(new URL(urlStr));
                    metadata.setId(jsonResponse.get("result").get("revision_id").textValue());
                    metadata.setFileExtension(jsonResponse.get("result").get("format").textValue().toLowerCase());
                    return metadata;

                } catch (IOException e) {
                    currentTry++;
                    if (currentTry >= stepParameters.requestRetries) {
                        throw e;
                    }
                }
            }
            return null;
        } catch (Exception e){
            throw new RuntimeException("Can't get actual file link for " + stepParameters.getSourceName());
        }

    }



    private String downloadFile(URL fileUrl, String filename, StepParameters stepParameters, TaskExecutionContextDto taskExecutionContextDto) throws IOException {
        taskExecutionContextDto.setComment("Зчитування актуального ресурсу");
        taskExecutionContextDto.setProgress(0.0);
        taskExecutionContextDto.setStatus(TaskStatus.IN_PROGRESS);
        sendStepUpdate(taskExecutionContextDto);

        File destination = new File(filename);

        long totalBytesRead = 0;
        int attempt = 0;
        long fileSize = getFileSize(fileUrl);

        try (RandomAccessFile destinationFile = new RandomAccessFile(destination, "rw")) {
            while (totalBytesRead < fileSize && attempt < stepParameters.getRequestRetries()) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
                    String byteRange = totalBytesRead + "-" + (fileSize - 1);
                    connection.setRequestProperty("Range", "bytes=" + byteRange);
                    connection.setReadTimeout(stepParameters.getRequestTimeoutSec() * 1000);

                    int lastPercentagePrinted = 0;
                    try (InputStream inputStream = connection.getInputStream()) {
                        byte[] buffer = new byte[1024 * 64];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            if (bytesRead == 0) {
                                System.out.println("No bytes read, possible stall in download.");
                                break;
                            }
                            destinationFile.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            int currentPercentage = (int) (100 * totalBytesRead / fileSize);
                            if (currentPercentage > lastPercentagePrinted) {
                                log.debug("Download progress: " + currentPercentage + "%");
                                String comment = "Завантажено " + totalBytesRead / 1000 / 1000 + " mb / " + fileSize / 1000 / 1000 + " mb";

                                taskExecutionContextDto.setComment(comment);
                                taskExecutionContextDto.setProgress((double) totalBytesRead / fileSize);
                                sendStepUpdate(taskExecutionContextDto);

                                lastPercentagePrinted = currentPercentage;
                            }
                        }
                    }
                    attempt = 0;
                } catch (IOException e) {
                    attempt++;
                    if (attempt >= stepParameters.getRequestRetries()) {
                        throw e;
                    }
                }
            }
        }
        taskExecutionContextDto.setComment("Завантажено " + totalBytesRead / 1000 / 1000 + " mb");
        sendStepUpdate(taskExecutionContextDto);
        return filename;
    }

    private long getFileSize(URL fileUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
        conn.setRequestMethod("HEAD");
        long length = conn.getContentLengthLong();
        conn.disconnect();
        return length;
    }

    private StepParameters readParameters(Long stepId, Map<String, String> parameters) throws IllegalArgumentException {
        StepParameters stepParams = new StepParameters();

        stepParams.setStepId(stepId);

        if (parameters.containsKey("packageId")) {
            stepParams.setPackageId(parameters.get("packageId"));
        } else {
            throw new IllegalArgumentException("Package ID is not present in StepRequest parameters!");
        }


        stepParams.setSourceName(parameters.getOrDefault("sourceNickName", "govua_undefined"));

        String retries = parameters.getOrDefault("retries", "3");
        try {
            stepParams.setRequestRetries(Integer.parseInt(retries));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid format for 'retries'. It should be an integer. Now its value: " + retries);
        }

        String timeout = parameters.getOrDefault("timeoutSec", "3");
        try {
            stepParams.setRequestTimeoutSec(Integer.parseInt(timeout));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid format for 'timeoutSec'. It should be an integer. Now its value: " + timeout);
        }
        return stepParams;
    }


    private void sendStepUpdate(TaskExecutionContextDto taskExecutionContextDto){
        StepUpdateDto stepUpdateDto = new StepUpdateDto();
        stepUpdateDto.setStepId(taskExecutionContextDto.getStepId());
        stepUpdateDto.setStatus(taskExecutionContextDto.getStatus());
        stepUpdateDto.setComment(taskExecutionContextDto.getComment());
        stepUpdateDto.setProgress(taskExecutionContextDto.getProgress());
        rabbitTemplate.convertAndSend("icms.cpms", stepUpdateDto);
    }


    @Data
    private class StepParameters {
        private Long stepId;
        private String packageId;
        private String sourceName;
        private Integer requestRetries;
        private Integer requestTimeoutSec;
    }

    @Data
    private class GovuaRevisionMetadata {
        private String id;
        private String fileExtension;
        private URL url;
    }

}
