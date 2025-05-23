package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Create
    @Transactional
    public Optional<UserDto> createUser(UserDto dto) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists: " + dto.getEmail());
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // В реальном приложении используйте хеширование!

        User saved = userRepository.save(user);
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // Update
    @Transactional
    public Optional<UserDto> updateUser(Long id, UserDto dto) {
        return userRepository.findById(id)
            .map(existing -> {
                // Проверка на изменение email и его уникальность
                if (!existing.getEmail().equals(dto.getEmail())) {
                    if (userRepository.existsByEmail(dto.getEmail())) {
                        throw new BadRequestException("Email already exists: " + dto.getEmail());
                    }
                }

                existing.setFullName(dto.getFullName());
                existing.setEmail(dto.getEmail());
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    existing.setPassword(dto.getPassword()); // Обновление без хеширования
                }
                User updated = userRepository.save(existing);
                return convertToDto(updated);
            });
    }

    // Delete
    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithReservationsOnDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Date cannot be null");
        }
        List<User> users = userRepository.findUsersWithReservationsOnDate(date);
        return users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // Convert to DTO
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPassword(null); // Не возвращаем пароль в DTO

        // Безопасная обработка reservations (защита от NPE)
        dto.setReservationIds(
            Optional.ofNullable(user.getReservations()) // Оборачиваем в Optional
                .orElseGet(Collections::emptyList)     // Если null - возвращаем пустой список
                .stream()
                .map(Reservation::getId)
                .collect(Collectors.toList())
        );

        return dto;
    }
}