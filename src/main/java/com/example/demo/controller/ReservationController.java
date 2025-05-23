package com.example.demo.controller;

import com.example.demo.dto.ReservationDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.ReservationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(@Valid@RequestBody ReservationDto dto) {
        if (dto.getId() != null) {
            throw new BadRequestException("ID should not be provided for creation");
        }
        if (dto.getReservationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        ReservationDto created = reservationService.createReservation(dto)
            .orElseThrow(() -> new BadRequestException("Failed to create reservation"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservation(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        ReservationDto reservation = reservationService.getReservationById(id)
            .orElseThrow(() -> new NotFoundException("Reservation not found with ID: " + id));
        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            throw new NotFoundException("No reservations found");
        }
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDto> updateReservation(
        @PathVariable Long id,
        @Valid @RequestBody ReservationDto dto) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        if (!id.equals(dto.getId())) {
            throw new BadRequestException("ID in path and body must match");
        }
        if (dto.getReservationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        ReservationDto updatedReservation = reservationService.updateReservation(id, dto)
            .orElseThrow(() -> new NotFoundException("Reservation not found with ID: " + id));
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        if (!reservationService.deleteReservation(id)) {
            throw new NotFoundException("Reservation not found with ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}