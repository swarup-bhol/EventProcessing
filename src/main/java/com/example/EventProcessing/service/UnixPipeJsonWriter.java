package com.example.EventProcessing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class UnixPipeJsonWriter {
    private static final int THREAD_COUNT = 4; // Adjust based on CPU cores
    private static final int TARGET_RATE = 10_000; // JSONs per second
    private static final int TOTAL_JSONS = 100_000; // Total JSONs to generate
    private static final String PIPE_PATH = "/tmp/json_pipe"; // Unix Named Pipe (FIFO)
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BlockingQueue<String> jsonQueue = new LinkedBlockingQueue<>(TARGET_RATE * 2);

    public static void main(String[] args) throws IOException, InterruptedException {
        createNamedPipe(PIPE_PATH);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Start JSON Generator
        executorService.submit(() -> {
            try {
                for (int i = 0; i < TOTAL_JSONS; i++) {
                    jsonQueue.put(objectMapper.writeValueAsString(generateJson(i + 1)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Start Pipe Writer Thread
        try (PrintWriter writer = new PrintWriter(new FileWriter(PIPE_PATH))) {
            while (true) {
                String json = jsonQueue.take();
                writer.println(json);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }

    // Create a Unix named pipe (FIFO) if it doesn't exist
    private static void createNamedPipe(String pipePath) throws IOException {
        Path path = Paths.get(pipePath);
        if (!Files.exists(path)) {
            new ProcessBuilder("mkfifo", pipePath).start();
            System.out.println("Created Unix named pipe: " + pipePath);
        }
    }

    // Generates JSON with exactly 10 fields
    private static Map<String, Object> generateJson(int id) {
        return Map.of(
                "id", id,
                "name", "User" + id,
                "email", "user" + id + "@example.com",
                "age", 20 + (id % 30),
                "isActive", id % 2 == 0,
                "createdAt", Instant.now().toString(),
                "address", Map.of("city", "City" + (id % 10), "zip", "1000" + (id % 10)),
                "phoneNumbers", List.of(Map.of("type", "home", "number", "123-456-" + (1000 + id % 9000))),
                "roles", List.of("User", (id % 5 == 0) ? "Admin" : "Member"),
                "subscription", Map.of("plan", (id % 2 == 0) ? "Premium" : "Basic", "renewalDate", "2025-12-01")
        );
    }
}
