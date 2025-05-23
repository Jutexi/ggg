package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto dto) {
        if (dto.getId() != null) {
            throw new BadRequestException("ID should not be provided for creation");
        }
        UserDto created = userService.createUser(dto)
            .orElseThrow(() -> new BadRequestException("Failed to create reservation"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        UserDto user = userService.getUserById(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new NotFoundException("No users found");
        }
        return ResponseEntity.ok(users);
    }

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
}