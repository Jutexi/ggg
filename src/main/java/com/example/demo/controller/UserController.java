package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Пользователи", description = "Операции с пользователями")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Создать нового пользователя")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto dto) {
        if (dto.getId() != null) {
            throw new BadRequestException("ID should not be provided for creation");
        }
        UserDto created = userService.createUser(dto)
            .orElseThrow(() -> new BadRequestException("Failed to create reservation"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Получить пользователя по {id}")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        UserDto user = userService.getUserById(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Получить всех пользователей")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new NotFoundException("No users found");
        }
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Обновить данные пользователя")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserDto dto) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        if (!id.equals(dto.getId())) {
            throw new BadRequestException("ID in path and body must match");
        }
        UserDto updatedUser = userService.updateUser(id, dto)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        if (!userService.deleteUser(id)) {
            throw new NotFoundException("User not found with ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить всех с бронированием на дату")
    @GetMapping("/with-reservations")
    public ResponseEntity<List<UserDto>> getUsersWithReservationsOnDate(
        @RequestParam("date") LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Date parameter is required");
        }
        List<UserDto> users = userService.getUsersWithReservationsOnDate(date);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found with reservations on date: " + date);
        }
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Создать несколько пользователей")
    @PostMapping("/bulk")
    public ResponseEntity<List<UserDto>> createUsersBulk(@Valid @RequestBody List<UserDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException("User list cannot be empty");
        }

        // Check if any IDs are provided (shouldn't be for creation)
        if (dtos.stream().anyMatch(dto -> dto.getId() != null)) {
            throw new BadRequestException("IDs should not be provided for creation");
        }

        // Check for duplicate emails in the request
        List<String> emails = dtos.stream().map(UserDto::getEmail).toList();
        if (emails.size() != new HashSet<>(emails).size()) {
            throw new BadRequestException("Duplicate emails in the request");
        }

        List<UserDto> createdUsers = userService.createUsersBulk(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
    }
}