package com.example.EventProcessing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class FastJsonGenerator {
    private static final int THREAD_COUNT = 4;
    private static final int TARGET_RATE = 10_000; // JSON objects per second
    private static final int TOTAL_JSONS = 100_000; // Total JSONs to generate
    private static final String FILE_PATH = "output.json"; // JSON file path
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BlockingQueue<String> jsonQueue = new LinkedBlockingQueue<>(TARGET_RATE * 2);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);


        executorService.submit(() -> {
            try {
                for (int i = 0; i < TOTAL_JSONS; i++) {
                    jsonQueue.put(objectMapper.writeValueAsString(generateJson(i + 1)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write("[\n");
            boolean first = true;
            while (!jsonQueue.isEmpty() || !executorService.isTerminated()) {
                String json = jsonQueue.poll(100, TimeUnit.MILLISECONDS);
                if (json != null) {
                    if (!first) writer.write(",\n");
                    writer.write(json);
                    first = false;
                }
            }
            writer.write("\n]");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("JSON writing to file completed: " + FILE_PATH);
    }

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