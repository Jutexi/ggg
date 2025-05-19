package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoworkingSpaceDto {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
    private List<Long> reservationIds;
}