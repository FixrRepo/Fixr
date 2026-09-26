package com.inspaya.fixr.rabbitMq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fixr.rabbit-mq")
public record RabbitConfigProperties(String fixrExchangeName, String requestQueue,String completedQueue,
                                     String requestQueueRk, String requestFailedRk, String completedQueueRk, String dlx, String dlq, String dlrk) {

}
