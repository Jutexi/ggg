package com.example.demo.cache;

import com.example.demo.entity.CoworkingSpace;
import org.springframework.stereotype.Component;

@Component
public class CoworkingSpaceCache extends LfuCache<CoworkingSpace> {
    public CoworkingSpaceCache() {
        super(100);
    }
}