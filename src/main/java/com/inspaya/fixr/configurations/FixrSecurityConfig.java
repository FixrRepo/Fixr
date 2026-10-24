package com.inspaya.fixr.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
import com.inspaya.fixr.security.details.FixrUserSecurityDetailsService;
import com.inspaya.fixr.security.filters.JwtAuthenticationFilter;
import com.inspaya.fixr.security.service.JwtUtil;
import com.inspaya.fixr.security.filters.JwtValidationFilter;
import com.inspaya.fixr.services.JwtTokenIssueManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(FixrUserSecurityProperties.class)
public class FixrSecurityConfig {
    private final FixrUserSecurityProperties properties;
    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;
    private final JwtTokenIssueManager tokenIssueManager;
    private final FixrUserSecurityDetailsService userSecurityDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http){
        JwtAuthenticationFilter authenticationFilter = new JwtAuthenticationFilter(
                mapper,
                authenticationProvider(),
                tokenIssueManager
        );
        authenticationFilter.setFilterProcessesUrl("/api/v1/fixr/user/auth/login");
        JwtValidationFilter validationFilter = new JwtValidationFilter(
                jwtUtil, userSecurityDetailsService
        );

        return http.httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request->{

                    request.requestMatchers(HttpMethod.POST,"/api/v1/fixr/user/auth/**").permitAll();
                    request.requestMatchers(HttpMethod.GET, "/api/v1/fixr/feed/**").permitAll();
                    request.requestMatchers(HttpMethod.GET, "/api/v1/fixr/users/*/reports").permitAll();
                    request.requestMatchers("/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll();
                    request.anyRequest().authenticated();
                })
                .anonymous(anonymousConfigurer -> {
                            anonymousConfigurer.principal("GuestUser");
                            anonymousConfigurer.authorities(List.of(new SimpleGrantedAuthority("GUEST")));
                    }
                )
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .addFilter(authenticationFilter)
                .addFilterBefore(validationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "PUT"));
        configuration.setMaxAge(3600L);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userSecurityDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder(16, 32, 8, 64,2);
    }
}
