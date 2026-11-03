package com.inspaya.fixr.configurations;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.configurations.properties.CloudinaryProperties;
import com.resend.Resend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.inspaya.fixr.configurations.properties.ValidationPolicyProperties;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

@Configuration
@EnableConfigurationProperties({ValidationPolicyProperties.class, CloudinaryProperties.class})
public class FixrConfig {
    @Autowired
    private CloudinaryProperties cloudinaryProperties;
    @Value("${fixr.email.resend.apiKey}")
    private String resendApiKey;

    @Bean
    JtsModule jtsModule(){
        return new JtsModule();
    }

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(Map.of(
                "cloud_name", cloudinaryProperties.cloudName(),
                "api_key", cloudinaryProperties.apiKey(),
                "api_secret", cloudinaryProperties.apiSecret()
        ));
    }

    @Bean
    public SpringTemplateEngine templateEngine(){
        SpringTemplateEngine engine = new SpringTemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Bean
    public Resend emailProvider(){
        return new Resend(resendApiKey);
    }
}
