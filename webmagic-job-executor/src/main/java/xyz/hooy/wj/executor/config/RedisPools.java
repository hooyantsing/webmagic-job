package xyz.hooy.wj.executor.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Setter
@Configuration
public class RedisPools {

    @Value("spring.redis.host")
    private String host;

    @Value("spring.redis.port")
    private String port;

    @Bean
    public JedisPool redisPool() {
        return new JedisPool(new JedisPoolConfig(), host);
    }
}
