package com.inspaya.fixr.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixr.cloudinary")
public record CloudinaryProperties(
        String apiKey,
        String apiSecret,
        String cloudName,
        String folderName
) {
}
