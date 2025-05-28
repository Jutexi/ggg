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
        user.setFirstName(dto.getFirstName());
        user.setMiddleName(dto.getMiddleName());
        user.setLastName(dto.getLastName());
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

                existing.setFirstName(dto.getFirstName());
                existing.setMiddleName(dto.getMiddleName());
                existing.setLastName(dto.getLastName());
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

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByCoworkingSpace(Long coworkingSpaceId) {
        if (coworkingSpaceId == null || coworkingSpaceId <= 0) {
            throw new BadRequestException("Invalid coworking space ID");
        }
        List<User> users = userRepository.findUsersByCoworkingSpace(coworkingSpaceId);
        users.forEach(user -> userCache.put(user.getId(), user)); // Кэшируем пользователей
        return users.stream()
            .map(this::convertToDto)
            .toList();
    }

    @Transactional
    public List<UserDto> createUsersBulk(List<UserDto> dtos) {
        // Check if any emails already exist in database
        List<String> emails = dtos.stream().map(UserDto::getEmail).toList();
        List<String> existingEmails = userRepository.findByEmailIn(emails).stream()
            .map(User::getEmail)
            .toList();

        if (!existingEmails.isEmpty()) {
            throw new BadRequestException("Emails already exist: " + existingEmails);
        }

        // Convert all DTOs to entities
        List<User> users = dtos.stream().map(dto -> {
            User user = new User();
            user.setFirstName(dto.getFirstName());
            user.setMiddleName(dto.getMiddleName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            return user;
        }).toList();

        // Save all users
        List<User> savedUsers = userRepository.saveAll(users);

        // Add to cache
        savedUsers.forEach(user -> userCache.put(user.getId(), user));

        // Convert to DTOs and return
        return savedUsers.stream()
            .map(this::convertToDto)
            .toList();
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
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