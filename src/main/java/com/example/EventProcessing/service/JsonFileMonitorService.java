package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Set;
import java.util.concurrent.*;

@Service
public class JsonFileMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileMonitorService.class);
    private static final String PIPE_PATH = "/tmp/json_pipe";

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Set<Integer> processedUserIds = ConcurrentHashMap.newKeySet();
    private long lastKnownSize = 0;

    public JsonFileMonitorService(AppConfig appConfig) {
        this.appConfig = appConfig;
        startMonitoring();
    }

    private void startMonitoring() {
        File inputFile = new File(appConfig.getInputFilePath());

        // Read existing users when the service starts
        if (inputFile.exists() && inputFile.length() > 0) {
            processFile();
        }

        // Schedule continuous checking every 2 seconds
        executorService.scheduleWithFixedDelay(this::processFile, 0, 2, TimeUnit.SECONDS);
    }

    private void processFile() {
        File inputFile = new File(appConfig.getInputFilePath());

        if (!inputFile.exists() || inputFile.length() == lastKnownSize) {
            return; // No changes detected
        }

        try {
            JsonNode rootNode = objectMapper.readTree(inputFile);
            JsonNode usersNode = rootNode.get("users");

            if (usersNode != null && usersNode.isArray()) {
                for (JsonNode userNode : usersNode) {
                    int userId = userNode.get("id").asInt();

                    if (!processedUserIds.contains(userId)) {
                        processedUserIds.add(userId);
                        logger.info("New user detected: {}", userNode);
                        writeToPipe(userNode);
                    }
                }
            }

            lastKnownSize = inputFile.length();
        } catch (IOException e) {
            logger.error("Error reading file", e);
        }
    }

    private void writeToPipe(JsonNode userNode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PIPE_PATH, true))) {
            writer.write(userNode.toString());
            writer.newLine();
            writer.flush();
            logger.info("✅ Written to pipe: {}", userNode);
        } catch (IOException e) {
            logger.error("❌ Error writing to pipe", e);
        }
    }

    public void stopMonitoring() {
        executorService.shutdown();
    }
}