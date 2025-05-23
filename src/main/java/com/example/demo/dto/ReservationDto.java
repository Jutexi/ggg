package com.example.demo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationDto {
    private Long id;

    @NotNull(message = "Reservation date is required")
    @FutureOrPresent(message = "Reservation date must be today or in the future")
    private LocalDate reservationDate;

    @NotNull(message = "Coworking space ID is required")
    private Long coworkingSpaceId;

    @NotEmpty(message = "At least one user ID must be specified")
    private List<@NotNull(message = "User ID cannot be null") Long> userIds;
}