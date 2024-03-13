package ua.com.valexa.downloader.service.govua;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service("govua12")
@Slf4j
public class Govua12Downloader implements Downloadable {

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

            
            Set<String> urls = parseFileUrls(packageMetadata);
            
//            taskExecutionContextDto.setComment("Зчитування актуального ресурсу");
//            sendStepUpdate(taskExecutionContextDto);
//            String actualResourceId = getActualResourceId(packageMetadata, stepParameters);

//            taskExecutionContextDto.setComment("Зчитування метадати ресурсу");
//            sendStepUpdate(taskExecutionContextDto);
//            GovuaRevisionMetadata metadata = getRevisionMetadata(actualResourceId, stepParameters);

//            String fileName = mountPoint + System.getProperty("file.separator") + stepRequestDto.getJobId() + "_" + stepParameters.getSourceName() + "." + metadata.getFileExtension();


            Map<String, String> urlFilesMap = new HashMap<>();
            String fileNameMerged = mountPoint + System.getProperty("file.separator") + stepRequestDto.getJobId() + "_" + stepParameters.getSourceName() + ".json";
            int i = 1;
            for (String u : urls){
                String fileName = mountPoint + System.getProperty("file.separator") + stepRequestDto.getJobId() + "_" + stepParameters.getSourceName() +"_part_" + i + ".json";
                i++;
                urlFilesMap.put(u, fileName);
            }

            int partCount = urlFilesMap.size();
            int currentPart = 1;

            for (String url : urlFilesMap.keySet()){
                downloadFile(new URL(url), urlFilesMap.get(url), stepParameters, taskExecutionContextDto, partCount, currentPart++);
            }

            mergeJsonFiles(urlFilesMap, fileNameMerged);



//            String downloadedFile = downloadFile(metadata.getUrl(), fileName, stepParameters, taskExecutionContextDto);

            stepResponseDto.getResults().put("file", fileNameMerged);
            stepResponseDto.setStatus(TaskStatus.FINISHED);
            stepResponseDto.setComment(taskExecutionContextDto.getComment());
        } catch (Exception e){
            log.error("Downloading failed : " + e.getMessage());
            stepResponseDto.setComment(e.getMessage());
            stepResponseDto.setStatus(TaskStatus.FAILED);
        }
        return stepResponseDto;
    }

    private static void mergeJsonFiles(Map<String, String> filePathsMap, String outputFilePath) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(new FileWriter(outputFilePath))) {
            jsonGenerator.writeStartArray(); // Start the JSON array in the output file

            for (String filePath : filePathsMap.values()) {
                try (JsonParser jsonParser = jsonFactory.createParser(new File(filePath))) {
                    if (jsonParser.nextToken() == JsonToken.START_ARRAY) {
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            jsonGenerator.copyCurrentStructure(jsonParser); // Copy each element to the output
                        }
                    }
                }
            }

            jsonGenerator.writeEndArray();

            for (String filePath : filePathsMap.values()) {
                try {
                    Path path = Paths.get(filePath);
                    Files.delete(path);
//                    System.out.println("Deleted: " + filePath);
                } catch (IOException e) {
//                    System.err.println("Could not delete file: " + filePath + ". Reason: " + e.getMessage());
                }
            }
            // End the JSON array
        }
    }

    private Set<String> parseFileUrls(JsonNode packageMetadata) {
        Set<String> urls = new HashSet<>();

        // Check if 'result' node is present and if 'resources' is an array
        if (packageMetadata != null && packageMetadata.has("result")) {
            JsonNode result = packageMetadata.get("result");
            if (result.has("resources") && result.get("resources").isArray()) {
                JsonNode resources = result.get("resources");

                // Loop through all elements in the 'resources' array except the last one
                for (int i = 0; i < resources.size() - 1; i++) {
                    JsonNode resource = resources.get(i);
                    if (resource.has("url")) {
                        urls.add(resource.get("url").asText());
                    }
                }
            }
        }

        return urls;
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



    private String downloadFile(URL fileUrl, String filename, StepParameters stepParameters, TaskExecutionContextDto taskExecutionContextDto, int partCount, int currentPart) throws IOException {
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
                                String comment = "Завантажено " + totalBytesRead / 1000 / 1000 + " mb / " + fileSize / 1000 / 1000 + " mb; ( Файл " + currentPart + "/" + partCount + ")" ;

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
