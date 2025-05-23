package com.example.demo.cache;

import com.example.demo.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationCache extends LfuCache<Reservation> {
  public ReservationCache() {
    super(100);
  }
}
