package com.inspaya.fixr.configurations.properties;

import com.inspaya.fixr.enums.AppProfile;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fixr.security")
public record FixrUserSecurityProperties(
        AppProfile profile,
        String secretKey,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays,
        String domain,
        List<String> allowedOrigins
) {

}
