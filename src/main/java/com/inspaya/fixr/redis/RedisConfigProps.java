package com.inspaya.fixr.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisConfigProps(
        String host,
        String password,
        int port
) {

}
