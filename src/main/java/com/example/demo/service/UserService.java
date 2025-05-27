package com.example.demo.service;

import com.example.demo.cache.UserCache;
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
    private final UserCache userCache;

    // Create
    @Transactional
    public Optional<UserDto> createUser(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists: " + dto.getEmail());
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        User saved = userRepository.save(user);
        userCache.put(saved.getId(), saved); // Добавляем в кэш
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        User cachedUser = userCache.get(id); // Проверяем кэш
        if (cachedUser != null) {
            return Optional.of(convertToDto(cachedUser));
        }

        return userRepository.findById(id)
            .map(user -> {
                userCache.put(user.getId(), user); // Кэшируем результат
                return convertToDto(user);
            });
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> userCache.put(user.getId(), user)); // Кэшируем всех пользователей
        return users.stream()
            .map(this::convertToDto)
            .toList();
    }

    // Update
    @Transactional
    public Optional<UserDto> updateUser(Long id, UserDto dto) {
        return userRepository.findById(id)
            .map(existing -> {
                if (!existing.getEmail().equals(dto.getEmail())
                    && userRepository.existsByEmail(dto.getEmail())) {
                    throw new BadRequestException("Email already exists: " + dto.getEmail());
                }

                existing.setFullName(dto.getFullName());
                existing.setEmail(dto.getEmail());
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    existing.setPassword(dto.getPassword());
                }
                User updated = userRepository.save(existing);
                userCache.put(updated.getId(), updated); // Обновляем кэш
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
        userCache.remove(id); // Удаляем из кэша
        return true;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithReservationsOnDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Date cannot be null");
        }
        List<User> users = userRepository.findUsersWithReservationsOnDate(date);
        users.forEach(user -> userCache.put(user.getId(), user)); // Кэшируем пользователей
        return users.stream()
            .map(this::convertToDto)
            .toList();
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPassword(null);
        dto.setReservationIds(
            Optional.ofNullable(user.getReservations())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Reservation::getId)
                .collect(Collectors.toList())
        );
        return dto;
    }
}