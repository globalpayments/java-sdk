package com.global.api.utils;

import com.global.api.logging.IRequestLogger;

import java.io.*;
import java.sql.Timestamp;

public class RequestFileLogger implements IRequestLogger {
    private String outputToFile;

    public RequestFileLogger(String outputFile) {
        this.outputToFile = outputFile;
    }

    @Override
    public void RequestSent(String request) throws IOException {
        long currentMillis = System.currentTimeMillis();
        Timestamp t = new Timestamp(currentMillis);

        try (BufferedWriter pw = new BufferedWriter(new FileWriter(outputToFile, true))) {
            pw.append(t + " Sent:\n" + request);
        }
    }

    @Override
    public void ResponseReceived(String response) throws IOException {
        long currentMillis = System.currentTimeMillis();
        Timestamp t = new Timestamp(currentMillis);

        try (BufferedWriter pw = new BufferedWriter(new FileWriter(outputToFile, true))) {
            pw.append(t + " Response:\n" + response + "\n");
        }
    }
}
