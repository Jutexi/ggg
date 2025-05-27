package com.example.demo.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import com.example.demo.service.LogService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/logs")
@Tag(name = "Log API", description = "API для синхронной и асинхронной генерации логов")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Асинхронно сгенерировать лог-файл по дате",
            description = "Запускает задачу по генерации лог-файла. Возвращает UUID задачи.")
    public ResponseEntity<String> generateLogFile(
            @Parameter(description = "Дата в формате dd.MM.yyyy", example = "23.04.2025")
            @RequestParam String date) {

        UUID id = logService.generateLogAsync(date);
        return ResponseEntity.ok(id.toString());
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "Проверить статус асинхронной задачи по UUID",
            description = "Позволяет узнать, завершена ли генерация лог-файла.")
    public ResponseEntity<String> getStatus(
            @Parameter(description = "UUID задачи")
            @PathVariable UUID id) {

        return ResponseEntity.ok(logService.getTaskStatus(id));
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Скачать лог-файл после асинхронной генерации",
            description = "Возвращает готовый лог-файл по UUID задачи.")
    public ResponseEntity<Resource> downloadGeneratedFile(
            @Parameter(description = "UUID задачи")
            @PathVariable UUID id) {

        Resource resource = logService.getGeneratedFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"log-" + id + ".log\"")
                .body(resource);
    }
}