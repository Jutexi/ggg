package com.example.demo.cache;

import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LfuCache<User> {
    public UserCache() {
        super(100);
    }
}