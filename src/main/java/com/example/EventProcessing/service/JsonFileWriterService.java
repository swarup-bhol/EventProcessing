package com.example.EventProcessing.service;

import com.example.EventProcessing.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class JsonFileWriterService {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonFileWriterService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public synchronized void writeToFile(JsonNode newUser) {
        File outputFile = new File(appConfig.getOutputFilePath());
        ArrayNode usersArray;

        try {
            // 1. Read existing JSON file
            if (outputFile.exists() && outputFile.length() > 0) {
                String existingContent = new String(Files.readAllBytes(outputFile.toPath()));
                JsonNode rootNode = objectMapper.readTree(existingContent);

                if (rootNode.has("users") && rootNode.get("users").isArray()) {
                    usersArray = (ArrayNode) rootNode.get("users");
                } else {
                    usersArray = objectMapper.createArrayNode();
                }
            } else {
                usersArray = objectMapper.createArrayNode();
            }

            // 2. Check for duplicates (based on ID)
            boolean exists = false;
            for (JsonNode user : usersArray) {
                if (user.get("id").asInt() == newUser.get("id").asInt()) {
                    exists = true;
                    break;
                }
            }

            // 3. Append only if the user is new
            if (!exists) {
                usersArray.add(newUser);
            }

            // 4. Wrap the array in a root object
            ObjectNode root = objectMapper.createObjectNode();
            root.set("users", usersArray);

            // 5. Write back to file (overwrite with new structured JSON)
            try (FileWriter writer = new FileWriter(outputFile, false)) { // Overwrite mode
                writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
