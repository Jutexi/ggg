package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationDateAndCoworkingSpaceId(@NotNull(message = "Reservation date is required") @FutureOrPresent(message = "Reservation date must be today or in the future") LocalDate reservationDate, @NotNull(message = "Coworking space ID is required") Long coworkingSpaceId);
}
