package com.inspaya.fixr.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.enums.AppProfile;
import com.inspaya.fixr.enums.AuthHeaderType;
import com.inspaya.fixr.dtos.JwtTokenResponseDto;
import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.security.service.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenIssueManager {
    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;
    private final JwtTokenService tokenService;
    private final FixrUserSecurityProperties properties;

    public void addTokensToHttpResponse(FixrUserSecurityDetail securityDetail, HttpServletResponse response) throws IOException {
        UUID accessTokenJti = JwtUtil.generateJti();
        String accessToken = jwtUtil.generateToken(securityDetail, accessTokenJti, JwtUtil.TokenType.AccessToken);
        ResponseCookie accessTokenCookie = ResponseCookie.from("AccessToken", accessToken)
                        .httpOnly(true)
                        .path("/")
                        .secure(properties.profile() != AppProfile.Dev)
                        .sameSite(Cookie.SameSite.LAX.attributeValue())
                        .maxAge(Duration.ofMinutes(properties.accessTokenExpirationMinutes()))
                        .domain(properties.domain())
                        .build();

        tokenService.createToken(securityDetail.getUsername(), accessTokenJti);

        UUID refreshTokenJti = JwtUtil.generateJti();
        String refreshToken = jwtUtil.generateToken(securityDetail, refreshTokenJti, JwtUtil.TokenType.RefreshToken);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true)
                .secure(properties.profile() != AppProfile.Dev)
                .path("/")
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .maxAge(Duration.ofDays(properties.refreshTokenExpirationDays()))
                .build();
        tokenService.createToken(securityDetail.getUsername(), refreshTokenJti);

        response.setHeader("Set-Cookie", accessTokenCookie.toString());
        response.setHeader("Set-Cookie", refreshTokenCookie.toString());
        response.getWriter().write(mapper.writeValueAsString(new JwtTokenResponseDto(
                AuthHeaderType.Bearer,
                accessToken,
                refreshToken
        )));
    }
}
