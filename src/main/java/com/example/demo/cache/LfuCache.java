package com.example.demo.cache;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LfuCache<T> {

  private static final Logger logger = LoggerFactory.getLogger(LfuCache.class);

  private final int maxCapacity;
  private final Map<Long, CacheEntry<T>> cache = new HashMap<>();

  protected static class CacheEntry<T> {
    T value;
    int frequency;

    CacheEntry(T value) {
      this.value = value;
      this.frequency = 1;
    }
  }

  protected LfuCache(int maxCapacity) {
    this.maxCapacity = maxCapacity;
    logger.info("LFUCache initialized with max capacity: " + maxCapacity);
  }

  public synchronized T get(Long id) {
    CacheEntry<T> entry = cache.get(id);
    if (entry != null) {
      entry.frequency++;
      logger.info("Cache hit for key: " + id + ", frequency: " + entry.frequency);
      return entry.value;
    }
    logger.info("Cache miss for key: " + id);
    return null;
  }

  public synchronized void put(Long id, T value) {
    if (cache.containsKey(id)) {
      CacheEntry<T> entry = cache.get(id);
      entry.value = value;
      entry.frequency++;
      logger.info("Cache update for key: " + id + ", frequency: " + entry.frequency);
    } else {
      if (cache.size() >= maxCapacity) {
        logger.info("Cache is full, evicting least frequently used entry");
        evictLeastFrequentlyUsed();
      }
      cache.put(id, new CacheEntry<>(value));
      logger.info("Cache put for key: " + id + ", frequency: 1");
    }
  }

  private void evictLeastFrequentlyUsed() {
    Long lfuKey = null;
    int minFrequency = Integer.MAX_VALUE;

    for (Map.Entry<Long, CacheEntry<T>> entry : cache.entrySet()) {
      if (entry.getValue().frequency < minFrequency) {
        minFrequency = entry.getValue().frequency;
        lfuKey = entry.getKey();
      }
    }
    if (lfuKey != null) {
      cache.remove(lfuKey);
      logger.info("Evicted key: " + lfuKey + " with frequency: " + minFrequency);
    }
  }

  public synchronized void remove(Long id) {
    cache.remove(id);
    logger.info("Removed key: " + id);
  }

  public synchronized void clear() {
    cache.clear();
    logger.info("Cache cleared");
  }
}