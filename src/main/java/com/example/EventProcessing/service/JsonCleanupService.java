package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class JsonCleanupService {

    private final AppConfig appConfig;

    public JsonCleanupService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void clearFile() {
        try (FileWriter writer = new FileWriter(appConfig.getOutputFilePath())) {
//            writer.write(""); // Clear file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
