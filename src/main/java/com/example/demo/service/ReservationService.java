package com.example.demo.service;

import com.example.demo.dto.ReservationDto;
import com.example.demo.entity.CoworkingSpace;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CoworkingSpaceRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CoworkingSpaceRepository coworkingSpaceRepository;
    private final UserRepository userRepository;

    // Create
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public Optional<ReservationDto> createReservation(ReservationDto dto) {
        // Проверка даты бронирования
        if (dto.getReservationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        // Проверка существования coworking space
        CoworkingSpace space = coworkingSpaceRepository.findById(dto.getCoworkingSpaceId())
            .orElseThrow(() -> new NotFoundException("Coworking space not found with ID: "
                + dto.getCoworkingSpaceId()));

        // Проверка существования всех пользователей
        List<User> users = userRepository.findAllById(dto.getUserIds());
        if (users.size() != dto.getUserIds().size()) {
            List<Long> foundIds = users.stream().map(User::getId).toList();
            List<Long> missingIds = dto.getUserIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
            throw new NotFoundException("Users not found with IDs: " + missingIds);
        }

        // Проверка доступности пространства на эту дату
        if (reservationRepository.existsByReservationDateAndCoworkingSpaceId(
            dto.getReservationDate(), dto.getCoworkingSpaceId())) {
            throw new BadRequestException("Coworking space is already reserved for this date");
        }

        // Создание бронирования
        Reservation reservation = new Reservation();
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setCoworkingSpace(space);
        reservation.setUsers(users);

        Reservation saved = reservationRepository.save(reservation);
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<ReservationDto> getReservationById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        return reservationRepository.findById(id)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // Update
    @Transactional
    public Optional<ReservationDto> updateReservation(Long id, ReservationDto dto) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        if (!id.equals(dto.getId())) {
            throw new BadRequestException("ID in path and body must match");
        }
        if (dto.getReservationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        return reservationRepository.findById(id)
            .map(existing -> {
                // Обновление даты
                existing.setReservationDate(dto.getReservationDate());

                // Обновление coworking space (если изменился)
                if (!existing.getCoworkingSpace().getId().equals(dto.getCoworkingSpaceId())) {
                    CoworkingSpace space = coworkingSpaceRepository.findById(
                        dto.getCoworkingSpaceId())
                        .orElseThrow(() -> new NotFoundException(
                                "Coworking space not found with ID: " + dto.getCoworkingSpaceId()));
                    existing.setCoworkingSpace(space);

                    // Проверка доступности нового пространства на эту дату
                    if (reservationRepository.existsByReservationDateAndCoworkingSpaceId(
                        dto.getReservationDate(), dto.getCoworkingSpaceId())) {
                        throw new BadRequestException(
                            "New coworking space is already reserved for this date");
                    }
                }

                // Обновление пользователей (если изменились)
                if (!new HashSet<>(existing.getUsers().stream()
                        .map(User::getId)
                        .collect(Collectors.toList()))
                    .containsAll(dto.getUserIds())) {
                    List<User> users = userRepository.findAllById(dto.getUserIds());
                    if (users.size() != dto.getUserIds().size()) {
                        List<Long> foundIds = users.stream().map(User::getId).toList();
                        List<Long> missingIds = dto.getUserIds().stream()
                            .filter(userId -> !foundIds.contains(userId))
                            .toList();
                        throw new NotFoundException("Users not found with IDs: " + missingIds);
                    }
                    existing.setUsers(users);
                }

                Reservation updated = reservationRepository.save(existing);
                return convertToDto(updated);
            });
    }

    // Delete
    @Transactional
    public boolean deleteReservation(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }
        if (!reservationRepository.existsById(id)) {
            return false;
        }
        reservationRepository.deleteById(id);
        return true;
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