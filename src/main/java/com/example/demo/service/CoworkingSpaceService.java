package com.example.demo.service;

import com.example.demo.dto.CoworkingSpaceDto;
import com.example.demo.entity.CoworkingSpace;
import com.example.demo.entity.Reservation;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CoworkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoworkingSpaceService {

    private final CoworkingSpaceRepository coworkingSpaceRepository;

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
        return Optional.of(convertToDto(saved));
    }

    // Read
    @Transactional(readOnly = true)
    public Optional<CoworkingSpaceDto> getSpaceById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid space ID");
        }

        return coworkingSpaceRepository.findById(id)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<CoworkingSpaceDto> getAllSpaces() {
        return coworkingSpaceRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
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
                // Проверка на уникальность имени, если оно изменилось
                if (!existing.getName().equals(dto.getName())) {
                    if (coworkingSpaceRepository.existsByName(dto.getName())) {
                        throw new BadRequestException("Space with name '" + dto.getName() + "' already exists");
                    }
                }

                existing.setName(dto.getName());
                existing.setAddress(dto.getAddress());
                CoworkingSpace updated = coworkingSpaceRepository.save(existing);
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
        return true;
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