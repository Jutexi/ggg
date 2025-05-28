package com.example.demo.service;

import com.example.demo.cache.ReservationCache;
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
    private final ReservationCache reservationCache;

    // Create
    @Transactional
    public Optional<ReservationDto> createReservation(ReservationDto dto) {
        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        List<User> users = userRepository.findAllById(dto.getUserIds());
        if (users.size() != dto.getUserIds().size()) {
            List<Long> foundIds = users.stream().map(User::getId).toList();
            List<Long> missingIds = dto.getUserIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
            throw new NotFoundException("Users not found with IDs: " + missingIds);
        }

        CoworkingSpace space = coworkingSpaceRepository.findById(dto.getCoworkingSpaceId())
            .orElseThrow(() -> new NotFoundException("Coworking space not found with ID: "
                + dto.getCoworkingSpaceId()));

        if (reservationRepository.existsByDateAndCoworkingSpaceId(
            dto.getDate(), dto.getCoworkingSpaceId())) {
            throw new BadRequestException("Coworking space is already reserved for this date");
        }

        Reservation reservation = new Reservation();
        reservation.setDate(dto.getDate());
        reservation.setCoworkingSpace(space);
        reservation.setUsers(users);

        Reservation saved = reservationRepository.save(reservation);
        reservationCache.put(saved.getId(), saved); // Добавляем в кэш
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<ReservationDto> getReservationById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid reservation ID");
        }

        Reservation cachedReservation = reservationCache.get(id); // Проверяем кэш
        if (cachedReservation != null) {
            return Optional.of(convertToDto(cachedReservation));
        }

        return reservationRepository.findById(id)
            .map(reservation -> {
                reservationCache.put(reservation.getId(), reservation); // Кэшируем результат
                return convertToDto(reservation);
            });
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        reservations.forEach(reservation ->
            reservationCache.put(reservation.getId(), reservation)); // Кэшируем все бронирования
        return reservations.stream()
            .map(this::convertToDto)
            .toList();
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
        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        return reservationRepository.findById(id)
            .map(existing -> {
                existing.setDate(dto.getDate());

                if (!existing.getCoworkingSpace().getId().equals(dto.getCoworkingSpaceId())) {
                    CoworkingSpace space = coworkingSpaceRepository.findById(
                        dto.getCoworkingSpaceId())
                        .orElseThrow(() -> new NotFoundException(
                            "Coworking space not found with ID: " + dto.getCoworkingSpaceId()));
                    existing.setCoworkingSpace(space);

                    if (reservationRepository.existsByDateAndCoworkingSpaceId(
                        dto.getDate(), dto.getCoworkingSpaceId())) {
                        throw new BadRequestException(
                            "New coworking space is already reserved for this date");
                    }
                }

                if (!new HashSet<>(existing.getUsers().stream().map(
                    User::getId).collect(Collectors.toList()))
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
                reservationCache.put(updated.getId(), updated); // Обновляем кэш
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
        reservationCache.remove(id); // Удаляем из кэша
        return true;
    }

    @Transactional
    public List<ReservationDto> createReservationsBulk(List<ReservationDto> dtos) {
        // Validate all dates are present or future
        if (dtos.stream().anyMatch(dto -> dto.getDate().isBefore(LocalDate.now()))) {
            throw new BadRequestException("Reservation date cannot be in the past");
        }

        // Get all unique user IDs from all reservations
        List<Long> allUserIds = dtos.stream()
            .flatMap(dto -> dto.getUserIds().stream())
            .distinct()
            .toList();

        // Get all users in one query
        List<User> allUsers = userRepository.findAllById(allUserIds);
        if (allUsers.size() != allUserIds.size()) {
            List<Long> foundUserIds = allUsers.stream().map(User::getId).toList();
            List<Long> missingUserIds = allUserIds.stream()
                .filter(id -> !foundUserIds.contains(id))
                .toList();
            throw new NotFoundException("Users not found with IDs: " + missingUserIds);
        }

        // Get all unique space IDs
        List<Long> spaceIds = dtos.stream()
            .map(ReservationDto::getCoworkingSpaceId)
            .distinct()
            .toList();

        // Get all spaces in one query
        List<CoworkingSpace> spaces = coworkingSpaceRepository.findAllById(spaceIds);
        if (spaces.size() != spaceIds.size()) {
            List<Long> foundSpaceIds = spaces.stream().map(CoworkingSpace::getId).toList();
            List<Long> missingSpaceIds = spaceIds.stream()
                .filter(id -> !foundSpaceIds.contains(id))
                .toList();
            throw new NotFoundException("Coworking spaces not found with IDs: " + missingSpaceIds);
        }

        // Check for existing reservations in one query
        List<LocalDate> dates = dtos.stream().map(ReservationDto::getDate).toList();
        List<Reservation> existingReservations = reservationRepository
            .findByDateInAndCoworkingSpaceIdIn(dates, spaceIds);

        if (!existingReservations.isEmpty()) {
            List<String> conflicts = existingReservations.stream()
                .map(r -> "Space ID " + r.getCoworkingSpace().getId() + " on " + r.getDate())
                .toList();
            throw new BadRequestException("Some spaces are already reserved for these dates: " + conflicts);
        }

        // Create all reservations
        List<Reservation> reservations = dtos.stream().map(dto -> {
            Reservation reservation = new Reservation();
            reservation.setDate(dto.getDate());

            CoworkingSpace space = spaces.stream()
                .filter(s -> s.getId().equals(dto.getCoworkingSpaceId()))
                .findFirst()
                .orElseThrow(); // Should never happen due to previous checks

            reservation.setCoworkingSpace(space);

            List<User> users = allUsers.stream()
                .filter(user -> dto.getUserIds().contains(user.getId()))
                .toList();

            reservation.setUsers(users);
            return reservation;
        }).toList();

        // Save all reservations
        List<Reservation> savedReservations = reservationRepository.saveAll(reservations);

        // Add to cache
        savedReservations.forEach(reservation ->
            reservationCache.put(reservation.getId(), reservation));

        // Convert to DTOs and return
        return savedReservations.stream()
            .map(this::convertToDto)
            .toList();
    }

    // Convert to DTO
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setDate(reservation.getDate());
        dto.setCoworkingSpaceId(reservation.getCoworkingSpace().getId());
        dto.setUserIds(
            reservation.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList())
        );
        return dto;
    }
}