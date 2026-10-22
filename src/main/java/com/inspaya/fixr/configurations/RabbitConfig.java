package com.inspaya.fixr.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.configurations.properties.RabbitConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(RabbitConfigProperties.class)
@EnableRabbit
@RequiredArgsConstructor
public class RabbitConfig {
    private final RabbitConfigProperties properties;

    @Bean(name = "fixrExchange")
    DirectExchange fixrExchange(){
        return ExchangeBuilder.directExchange(properties.fixrExchangeName())
                .durable(true)
                .build();
    }

    @Bean(name = "dlx")
    DirectExchange deadLetterExchange(){
        return ExchangeBuilder.directExchange(properties.dlx())
                .durable(true)
                .build();
    }

    @Bean(name = "requestQueue")
    Queue embeddingRequestQueue(){
        return QueueBuilder.durable(properties.requestQueue())
                .quorum()
                .deadLetterRoutingKey(properties.dlrk())
                .deadLetterExchange(properties.dlx())
                .build();
    }

    @Bean(name = "completedQueue")
    Queue completedQueue(){
        return QueueBuilder.durable(properties.completedQueue())
                .quorum()
                .build();
    }

    @Bean(name = "dlq")
    Queue deadLetterQueue(){
        return QueueBuilder.durable(properties.dlq())
                .quorum()
                .build();
    }

    @Bean(name = "notification")
    Queue notificationQueue(){
        return QueueBuilder.durable("notification")
                .quorum()
                .deadLetterExchange(properties.dlx())
                .deadLetterExchange(properties.dlrk())
                .build();
    }

    @Bean
    Binding fixrRequestQueueBinding(@Qualifier("requestQueue") Queue queue, @Qualifier("fixrExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(properties.requestQueueRk());
    }
    @Bean
    Binding fixrCompletedBinding(@Qualifier("completedQueue") Queue queue, @Qualifier("fixrExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(properties.completedQueueRk());
    }

    @Bean
    Binding fixrNotificationBoundToFixrExchange(@Qualifier("notification") Queue notificationQueue, @Qualifier("fixrExchange") DirectExchange exchange){
        return BindingBuilder.bind(notificationQueue).to(exchange).with(properties.completedQueueRk());
    }

    @Bean
    Binding deadLetterBinding(@Qualifier("dlq") Queue queue, @Qualifier("dlx") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(properties.dlrk());
    }

    @Bean
    Binding deadLetterBinding2(@Qualifier("dlq") Queue queue, @Qualifier("fixrExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(properties.requestFailedRk());
    }

    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter,
                                                                                     PlatformTransactionManager transactionManager){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setTransactionManager(transactionManager);
        return factory;
    }
}
