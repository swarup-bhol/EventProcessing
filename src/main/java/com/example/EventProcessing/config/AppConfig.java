package com.example.EventProcessing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class AppConfig {

    @Value("${file.input-path}")
    private String inputFilePath;

    @Value("${file.output-path}")
    private String outputFilePath;

    @Value("${kafka.topic-name}")
    private String kafkaTopic;

    String homeDir = System.getProperty("user.home");
    File inputFile = new File(homeDir + "/Desktop/input.json");
    File outputFile = new File(homeDir + "/Documents/output.json");
    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public String getInputFilePath() {
        return inputFile.toString();
    }

    public String getOutputFilePath() {
        return outputFile.toString();
    }
}
