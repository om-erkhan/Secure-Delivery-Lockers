package com.SecurityLockers.SecureDeliveryLockers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class RedisTestController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/redis")
    public String testRedis() {
        redisTemplate.opsForValue().set("testKey", "HelloRedis");
        return redisTemplate.opsForValue().get("testKey");
    }
}
