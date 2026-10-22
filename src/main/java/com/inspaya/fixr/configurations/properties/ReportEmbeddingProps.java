package com.inspaya.fixr.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixr.report.embedding")
public record ReportEmbeddingProps(
        double proximityThreshold,
        double distanceWeight,
        double textWeight,
        double visualsWeight
) {
}
