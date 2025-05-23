package com.example.demo.repository;

import com.example.demo.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Size(max = 255, message = "Email must be less than 255 characters") String email);

  @Query("SELECT DISTINCT u FROM User u JOIN u.reservations r WHERE r.reservationDate = :date")
  List<User> findUsersWithReservationsOnDate(@Param("date") LocalDate date);
}