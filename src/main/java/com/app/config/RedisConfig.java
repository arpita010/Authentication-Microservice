package com.app.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    //    return RedisCacheManager.builder(redisConnectionFactory).build();
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(10)) // optional TTL
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

    return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
  }

  //  @Bean
  //  JedisConnectionFactory jedisConnectionFactory() {
  //    JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
  //    jedisConFactory.setHostName("localhost");
  //    jedisConFactory.setPort(6379);
  //    return jedisConFactory;
  //  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  //  @Bean
  //  public RedisTemplate<String, Object> redisTemplate() {
  //    RedisTemplate<String, Object> template = new RedisTemplate<>();
  //    template.setConnectionFactory(redisConnectionFactory());
  //    return template;
  //  }
}
