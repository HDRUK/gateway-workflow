package com.gateway.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

@Configuration
public class RedisConfigureAction {
    @Bean
    public ConfigureRedisAction configurationRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
