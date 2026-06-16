package com.boardify.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final StringRedisTemplate redisTemplate;

  // 저장
  public void save(String key, String value, long duration, TimeUnit timeUnit) {
    redisTemplate.opsForValue().set(key, value, duration, timeUnit);
  }

  // 조회
  public String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  // 삭제
  public void delete(String key) {
    redisTemplate.delete(key);
  }

}
