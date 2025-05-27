package com.example.demo.controller;

import com.example.demo.dto.CoworkingSpaceDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.CoworkingSpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Пространства", description = "Операции с коворкинг-пространствами")
@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class CoworkingSpaceController {

    private final CoworkingSpaceService spaceService;

    @Operation(summary = "Создать новое коворкинг-пространство")
    @PostMapping
    public ResponseEntity<CoworkingSpaceDto> createSpace(@Valid@RequestBody CoworkingSpaceDto dto) {
        if (dto.getId() != null) {
            throw new BadRequestException("ID should not be provided for creation");
        }
        CoworkingSpaceDto created = spaceService.createSpace(dto)
            .orElseThrow(() -> new BadRequestException("Failed to create space"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Получить коворкинг-пространство по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CoworkingSpaceDto> getSpace(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }
        CoworkingSpaceDto space = spaceService.getSpaceById(id)
            .orElseThrow(() -> new NotFoundException("Space not found with ID: " + id));
        return ResponseEntity.ok(space);
    }

    @Operation(summary = "Получить все коворкинг-пространства")
    @GetMapping
    public ResponseEntity<List<CoworkingSpaceDto>> getAllSpaces() {
        List<CoworkingSpaceDto> spaces = spaceService.getAllSpaces();
        if (spaces.isEmpty()) {
            throw new NotFoundException("No spaces found");
        }
        return ResponseEntity.ok(spaces);
    }

    @Operation(summary = "Обновить коворкинг-пространство по ID")
    @PutMapping("/{id}")
    public ResponseEntity<CoworkingSpaceDto> updateSpace(
        @PathVariable Long id,
        @Valid @RequestBody CoworkingSpaceDto dto) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }
        if (!id.equals(dto.getId())) {
            throw new BadRequestException("ID in path and body must match");
        }
        CoworkingSpaceDto updatedSpace = spaceService.updateSpace(id, dto)
            .orElseThrow(() -> new NotFoundException("Space not found with ID: " + id));
        return ResponseEntity.ok(updatedSpace);
    }

    @Operation(summary = "Удалить коворкинг-пространство по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }
        if (!spaceService.deleteSpace(id)) {
            throw new NotFoundException("Space not found with ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}