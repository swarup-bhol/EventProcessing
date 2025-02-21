package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Service
public class KafkaProducerService {

//    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AppConfig appConfig;
    private final JsonCleanupService jsonCleanupService;

    public KafkaProducerService( AppConfig appConfig, JsonCleanupService jsonCleanupService) {
//        this.kafkaTemplate = kafkaTemplate;
        this.appConfig = appConfig;
        this.jsonCleanupService = jsonCleanupService;
    }

    public void processOutputFile() {
        File outputFile = new File(appConfig.getOutputFilePath());
        if (!outputFile.exists()) return;

        try {
            List<String> lines = Files.readAllLines(outputFile.toPath());
            for (String json : lines) {
                System.out.println(json);
//                kafkaTemplate.send(appConfig.getKafkaTopic(), json);
            }
            jsonCleanupService.clearFile(); // Delete processed lines
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

