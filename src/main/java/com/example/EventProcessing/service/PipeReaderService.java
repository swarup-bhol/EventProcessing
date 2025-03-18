package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PipeReaderService {
    private static final Logger logger = LoggerFactory.getLogger(PipeReaderService.class);
    private static final String PIPE_PATH = "/tmp/json_pipe"; // Path to the FIFO

    @Autowired
    AppConfig appConfig;
    @Scheduled(fixedDelay = 1000) // Poll every second
    public void monitorFifoPipe() {
        Path pipe = Paths.get(PIPE_PATH);
        if (!Files.exists(pipe)) {
            System.err.println("Pipe does not exist. Create it using: mkfifo " + PIPE_PATH);
            return;
        }

        System.out.println("Monitoring Unix Pipe: " + PIPE_PATH);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(PIPE_PATH)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(appConfig.getOutputFilePath(), true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}