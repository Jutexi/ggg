package com.example.demo.controller;

import com.example.demo.dto.CoworkingSpaceDto;
import com.example.demo.service.CoworkingSpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class CoworkingSpaceController {

    private final CoworkingSpaceService spaceService;

    @PostMapping
    public ResponseEntity<CoworkingSpaceDto> createSpace(@Valid @RequestBody CoworkingSpaceDto dto) {
        CoworkingSpaceDto created = spaceService.createSpace(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoworkingSpaceDto> getSpace(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getSpaceById(id));
    }

    @GetMapping
    public ResponseEntity<List<CoworkingSpaceDto>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoworkingSpaceDto> updateSpace(
            @PathVariable Long id,
            @Valid @RequestBody CoworkingSpaceDto dto) {
        return ResponseEntity.ok(spaceService.updateSpace(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }
}