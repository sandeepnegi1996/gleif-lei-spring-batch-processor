package com.hashcodehub.gleifleispringbatchprocessor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service to track and log failed LEI IDs and URLs to dedicated output files.
 */
@Service
public class FailedLeiTrackerService {

    @Value("${gleif.output.failed-records}")
    private String failedRecordsPath;

    private static final String FAILED_URLS_FILE = "failed_urls.log";

    /**
     * Logs a failed LEI record with the LEI ID and a reason to a dedicated CSV file.
     *
     * @param leiId The ID that failed processing.
     * @param reason The reason for the failure.
     */
    public void logFailedLei(String leiId, String reason) {
        String logEntry = String.format("%s,%s%n", leiId, reason);
        appendToFile(failedRecordsPath, logEntry);
    }

    /**
     * Logs a failed URL and reason to a separate log file for internal debugging.
     *
     * @param url The URL that failed.
     * @param reason The reason for the failure.
     */
    public void logFailedUrl(String url, String reason) {
        String logEntry = String.format("[%s] Failed URL: %s, Reason: %s%n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                url, reason);
        appendToFile(FAILED_URLS_FILE, logEntry);
    }

    /**
     * Appends a log entry to a specified file, ensuring the directory exists.
     *
     * @param filePath The name of the file.
     * @param content The content to append.
     */
    private void appendToFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Creates the 'output' directory if it doesn't exist

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.err.println("Could not write to file " + filePath + ". Reason: " + e.getMessage());
        }
    }
}