package com.example.EventProcessing.service;

import java.io.*;

public class PipeReader {
    private static final String PIPE_PATH = "/tmp/json_pipe";

    public static void main(String[] args) {
        File pipe = new File(PIPE_PATH);

        if (!pipe.exists()) {
            System.err.println("Error: Pipe does not exist!");
            return;
        }

        System.out.println("Listening to pipe: " + PIPE_PATH);

        try (FileInputStream fis = new FileInputStream(pipe);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while (true) {  // Keep reading from the pipe
                line = reader.readLine();
                if (line != null) {
                    System.out.println("Received: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


