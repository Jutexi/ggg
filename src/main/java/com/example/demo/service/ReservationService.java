// ReservationService
package com.example.demo.service;

import com.example.demo.dto.ReservationDto;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.CoworkingSpace;
import com.example.demo.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.CoworkingSpaceRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CoworkingSpaceRepository coworkingSpaceRepository;
    private final UserRepository userRepository;

    // Create
    @Transactional
    public ReservationDto createReservation(ReservationDto dto) {
        CoworkingSpace space = coworkingSpaceRepository.findById(dto.getCoworkingSpaceId())
                .orElseThrow(() -> new RuntimeException("Coworking space not found"));

        List<User> users = userRepository.findAllById(dto.getUserIds());
        if (users.isEmpty()) {
            throw new RuntimeException("No valid users found");
        }

        Reservation reservation = new Reservation();
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setCoworkingSpace(space);
        reservation.setUsers(users);

        Reservation saved = reservationRepository.save(reservation);
        return convertToDto(saved);
    }

    // Read
    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return convertToDto(reservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update
    @Transactional
    public ReservationDto updateReservation(Long id, ReservationDto dto) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        existing.setReservationDate(dto.getReservationDate());

        if (dto.getCoworkingSpaceId() != null) {
            CoworkingSpace space = coworkingSpaceRepository.findById(dto.getCoworkingSpaceId())
                    .orElseThrow(() -> new RuntimeException("Coworking space not found"));
            existing.setCoworkingSpace(space);
        }

        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            List<User> users = userRepository.findAllById(dto.getUserIds());
            existing.setUsers(users);
        }

        Reservation updated = reservationRepository.save(existing);
        return convertToDto(updated);
    }

    // Delete
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    // Convert to DTO
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setReservationDate(reservation.getReservationDate());
        dto.setCoworkingSpaceId(reservation.getCoworkingSpace().getId());
        dto.setUserIds(
                reservation.getUsers().stream()
                        .map(User::getId)
                        .collect(Collectors.toList())
        );
        return dto;
    }
}