package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndCoworkingSpaceId(
        @NotNull(message = "Reservation date is required")
        @FutureOrPresent(message = "Reservation date must be today or in the future")
        LocalDate date,
        @NotNull(message = "Coworking space ID is required") Long coworkingSpaceId);

    List<Reservation> findByDateInAndCoworkingSpaceIdIn(List<LocalDate> dates, List<Long> spaceIds);
}
