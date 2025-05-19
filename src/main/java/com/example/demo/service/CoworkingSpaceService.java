package com.example.demo.service;

import com.example.demo.dto.CoworkingSpaceDto;
import com.example.demo.entity.CoworkingSpace;
import com.example.demo.entity.Reservation;
import com.example.demo.repository.CoworkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoworkingSpaceService {

    private final CoworkingSpaceRepository coworkingSpaceRepository;

    // Create
    public CoworkingSpaceDto createSpace(CoworkingSpaceDto dto) {
        CoworkingSpace space = new CoworkingSpace();
        space.setName(dto.getName());
        space.setAddress(dto.getAddress());
        CoworkingSpace saved = coworkingSpaceRepository.save(space);
        return convertToDto(saved);
    }

    // Read
    public CoworkingSpaceDto getSpaceById(Long id) {
        CoworkingSpace space = coworkingSpaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found"));
        return convertToDto(space);
    }

    public List<CoworkingSpaceDto> getAllSpaces() {
        return coworkingSpaceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update
    public CoworkingSpaceDto updateSpace(Long id, CoworkingSpaceDto dto) {
        CoworkingSpace existing = coworkingSpaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found"));
        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        CoworkingSpace updated = coworkingSpaceRepository.save(existing);
        return convertToDto(updated);
    }

    // Delete
    public void deleteSpace(Long id) {
        coworkingSpaceRepository.deleteById(id);
    }

    // Convert to DTO
    private CoworkingSpaceDto convertToDto(CoworkingSpace space) {
        CoworkingSpaceDto dto = new CoworkingSpaceDto();
        dto.setId(space.getId());
        dto.setName(space.getName());
        dto.setAddress(space.getAddress());

        // Проверка на null перед вызовом stream()
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