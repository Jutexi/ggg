package com.example.demo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.LoggingException;
import com.example.demo.exception.NotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private static final String LOGS_DIR = "logs";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Map<UUID, CompletableFuture<File>> taskMap = new ConcurrentHashMap<>();

    public UUID generateLogAsync(String date) {
        UUID id = UUID.randomUUID();
        taskMap.put(id, CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
                return createLogFileForDate(date, id);
            } catch (IOException | InterruptedException e) {
                throw new LoggingException("Error generating log file");
            }
        }));
        return id;
    }

    public String getTaskStatus(UUID id) {
        CompletableFuture<File> future = taskMap.get(id);
        if (future == null) {
            return "NOT_FOUND";
        }
        return future.isDone() ? "COMPLETED" : "IN_PROGRESS";
    }

    public Resource getGeneratedFile(UUID id) {
        CompletableFuture<File> future = taskMap.get(id);
        if (future == null || !future.isDone()) {
            throw new NotFoundException("Log file not ready or does not exist");
        }
        try {
            byte[] content = Files.readAllBytes(future.get().toPath());
            return new ByteArrayResource(content);
        } catch (Exception e) {
            throw new LoggingException("Error reading generated file");
        }
    }

    private File createLogFileForDate(String date, UUID id) throws IOException {
        LocalDate parsedDate = parseDate(date);
        String formattedDate = parsedDate.format(LOG_DATE_FORMATTER);

        Path logFilePath = Paths.get(LOGS_DIR, "app.log");
        if (!Files.exists(logFilePath)) {
            throw new NotFoundException("Log file does not exist.");
        }

        List<String> filteredLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) {
                    filteredLines.add(line);
                }
            }
        }

        if (filteredLines.isEmpty()) {
            throw new NotFoundException("No logs found for this date.");
        }

        Path outputDir = Paths.get(LOGS_DIR, "generated");
        Files.createDirectories(outputDir);
        File outputFile = outputDir.resolve("log-" + id + ".log").toFile();
        Files.write(outputFile.toPath(), filteredLines, StandardCharsets.UTF_8);

        return outputFile;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Incorrect date format. Use dd.MM.yyyy.");
        }
    }
}