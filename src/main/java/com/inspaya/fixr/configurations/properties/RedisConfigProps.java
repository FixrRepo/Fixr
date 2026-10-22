package com.inspaya.fixr.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisConfigProps(
        String host,
        String password,
        int port
) {

}
