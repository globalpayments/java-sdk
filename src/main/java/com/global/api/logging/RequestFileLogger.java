package com.global.api.logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RequestFileLogger extends RequestConsoleLogger {
    private final File file;

    public RequestFileLogger(String fileName) {
        file = new File(fileName);
    }

    @Override
    public void RequestSent(String request){
        // Creates parent directories that do not exist. Doesn't throw an exception if the directories already exists
        try {
            Files.createDirectories(Paths.get(String.valueOf(file)).getParent());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.append(super.requestFormat(request)).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ResponseReceived(String response) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.append(super.responseFormat(response)).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}