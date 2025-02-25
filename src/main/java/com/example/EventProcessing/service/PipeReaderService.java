package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

@Service
public class PipeReaderService {
    private static final Logger logger = LoggerFactory.getLogger(PipeReaderService.class);
    private static final String PIPE_PATH = "/tmp/json_pipe";

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<Integer> existingUserIds = new HashSet<>();

    public PipeReaderService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void init() {
        ensureOutputFileExists();
        loadExistingUserIds();
        startReadingFromPipe();
    }

    private void ensureOutputFileExists() {
        File outputFile = new File(appConfig.getOutputFilePath());
        if (!outputFile.exists()) {
            try {
                outputFile.getParentFile().mkdirs();
                objectMapper.writeValue(outputFile, objectMapper.createArrayNode());
            } catch (IOException e) {
                logger.error("❌ Error creating output file", e);
            }
        }
    }

    private void loadExistingUserIds() {
        try {
            File outputFile = new File(appConfig.getOutputFilePath());
            JsonNode existingData = objectMapper.readTree(outputFile);

            if (existingData.isArray()) {
                for (JsonNode user : existingData) {
                    existingUserIds.add(user.get("id").asInt());
                }
            }
        } catch (IOException e) {
            logger.error("❌ Error loading existing users from output file", e);
        }
    }

    private void startReadingFromPipe() {
        Thread thread = new Thread(() -> {
            while (true) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PIPE_PATH)))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && isValidJson(line)) {
                            JsonNode newUser = objectMapper.readTree(line);
                            int userId = newUser.get("id").asInt();

                            if (!existingUserIds.contains(userId)) {
                                existingUserIds.add(userId);
                                writeToOutputFile(newUser);
                            } else {
                                logger.warn("⚠️ Duplicate user skipped: ID {}", userId);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("❌ Error reading from pipe", e);
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void writeToOutputFile(JsonNode newUser) {
        try {
            File outputFile = new File(appConfig.getOutputFilePath());
            JsonNode existingData = objectMapper.readTree(outputFile);
            ArrayNode updatedData = objectMapper.createArrayNode();

            if (existingData.isArray()) {
                updatedData.addAll((ArrayNode) existingData);
            }

            updatedData.add(newUser);
            objectMapper.writeValue(outputFile, updatedData);
            logger.info("✅ Successfully wrote to output file: {}", newUser);
        } catch (IOException e) {
            logger.error("❌ Error writing to output file", e);
        }
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
