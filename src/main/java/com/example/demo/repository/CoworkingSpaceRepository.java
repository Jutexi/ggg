package com.example.demo.repository;

import com.example.demo.entity.CoworkingSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoworkingSpaceRepository extends JpaRepository<CoworkingSpace, Long> {

    boolean existsByName(@NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be less than 100 characters") String name);
}