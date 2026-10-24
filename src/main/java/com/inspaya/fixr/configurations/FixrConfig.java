package com.inspaya.fixr.configurations;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.inspaya.fixr.configurations.properties.ValidationPolicyProperties;

@Configuration
@EnableConfigurationProperties(ValidationPolicyProperties.class)
public class FixrConfig {

    @Bean
    JtsModule jtsModule(){
        return new JtsModule();
    }

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
