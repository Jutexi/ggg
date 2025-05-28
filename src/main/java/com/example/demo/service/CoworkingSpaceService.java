package com.example.demo.service;

import com.example.demo.cache.CoworkingSpaceCache;
import com.example.demo.dto.CoworkingSpaceDto;
import com.example.demo.entity.CoworkingSpace;
import com.example.demo.entity.Reservation;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.CoworkingSpaceRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoworkingSpaceService {

    private final CoworkingSpaceRepository coworkingSpaceRepository;
    private final CoworkingSpaceCache coworkingSpaceCache;

    // Create
    @Transactional
    public Optional<CoworkingSpaceDto> createSpace(CoworkingSpaceDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BadRequestException("Space name cannot be empty");
        }

        if (coworkingSpaceRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Space with name '" + dto.getName() + "' already exists");
        }

        CoworkingSpace space = new CoworkingSpace();
        space.setName(dto.getName());
        space.setAddress(dto.getAddress());

        CoworkingSpace saved = coworkingSpaceRepository.save(space);
        coworkingSpaceCache.put(saved.getId(), saved); // Добавляем в кэш
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<CoworkingSpaceDto> getSpaceById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }

        CoworkingSpace cachedSpace = coworkingSpaceCache.get(id); // Проверяем кэш
        if (cachedSpace != null) {
            return Optional.of(convertToDto(cachedSpace));
        }

        return coworkingSpaceRepository.findById(id)
            .map(space -> {
                coworkingSpaceCache.put(space.getId(), space); // Кэшируем результат
                return convertToDto(space);
            });
    }

    @Transactional(readOnly = true)
    public List<CoworkingSpaceDto> getAllSpaces() {
        List<CoworkingSpace> spaces = coworkingSpaceRepository.findAll();
        spaces.forEach(space ->
            coworkingSpaceCache.put(space.getId(), space)); // Кэшируем все пространства
        return spaces.stream()
            .map(this::convertToDto)
            .toList();
    }

    // Update
    @Transactional
    public Optional<CoworkingSpaceDto> updateSpace(Long id, CoworkingSpaceDto dto) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }

        if (!id.equals(dto.getId())) {
            throw new BadRequestException("ID in path and body must match");
        }

        return coworkingSpaceRepository.findById(id)
            .map(existing -> {
                if (!existing.getName().equals(dto.getName())
                    && coworkingSpaceRepository.existsByName(dto.getName())) {
                    throw new BadRequestException("Space with name '"
                        + dto.getName()
                        + "' already exists");
                }

                existing.setName(dto.getName());
                existing.setAddress(dto.getAddress());
                CoworkingSpace updated = coworkingSpaceRepository.save(existing);
                coworkingSpaceCache.put(updated.getId(), updated); // Обновляем кэш
                return convertToDto(updated);
            });
    }

    // Delete
    @Transactional
    public boolean deleteSpace(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }

        if (!coworkingSpaceRepository.existsById(id)) {
            return false;
        }

        coworkingSpaceRepository.deleteById(id);
        coworkingSpaceCache.remove(id); // Удаляем из кэша
        return true;
    }
    @Transactional
    public List<CoworkingSpaceDto> createSpacesBulk(List<CoworkingSpaceDto> dtos) {
        // Check for empty names
        if (dtos.stream().anyMatch(dto -> dto.getName() == null || dto.getName().trim().isEmpty())) {
            throw new BadRequestException("Space name cannot be empty");
        }

        // Check if any names already exist in database
        List<String> names = dtos.stream().map(CoworkingSpaceDto::getName).toList();
        List<String> existingNames = coworkingSpaceRepository.findByNameIn(names).stream()
            .map(CoworkingSpace::getName)
            .toList();

        if (!existingNames.isEmpty()) {
            throw new BadRequestException("Spaces with these names already exist: " + existingNames);
        }

        // Convert all DTOs to entities
        List<CoworkingSpace> spaces = dtos.stream().map(dto -> {
            CoworkingSpace space = new CoworkingSpace();
            space.setName(dto.getName());
            space.setAddress(dto.getAddress());
            return space;
        }).toList();

        // Save all spaces
        List<CoworkingSpace> savedSpaces = coworkingSpaceRepository.saveAll(spaces);

        // Add to cache
        savedSpaces.forEach(space -> coworkingSpaceCache.put(space.getId(), space));

        // Convert to DTOs and return
        return savedSpaces.stream()
            .map(this::convertToDto)
            .toList();
    }

    // Convert to DTO
    private CoworkingSpaceDto convertToDto(CoworkingSpace space) {
        CoworkingSpaceDto dto = new CoworkingSpaceDto();
        dto.setId(space.getId());
        dto.setName(space.getName());
        dto.setAddress(space.getAddress());

        dto.setReservationIds(
            space.getReservations() == null
                ? Collections.emptyList()
                : space.getReservations().stream()
                .map(Reservation::getId)
                .collect(Collectors.toList())
        );

        return dto;
    }
}