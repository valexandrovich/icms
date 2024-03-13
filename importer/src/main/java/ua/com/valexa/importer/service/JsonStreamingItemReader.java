package ua.com.valexa.importer.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonStreamingItemReader<T> implements ItemReader<T> {

    private final String path;
    private final Class<T> targetType;
    private JsonParser jsonParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonStreamingItemReader(String path, Class<T> targetType) {
        this.path = path;
        this.targetType = targetType;
        init();
    }

    private void init() {
        try {
            jsonParser = new JsonFactory().createParser(Files.newInputStream(Paths.get(path)));
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize json parser", e);
        }
    }

    @Override
    public T read() throws Exception {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
//            System.out.println(";");
            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                return objectMapper.readValue(jsonParser, targetType);
            }
        }
        return null; // End of data
    }
}