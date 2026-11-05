package com.inspaya.fixr.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.enums.AppProfile;
import com.inspaya.fixr.enums.AuthHeaderType;
import com.inspaya.fixr.dtos.JwtTokenResponseDto;
import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
import com.inspaya.fixr.exceptions.CredentialsException;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.security.details.FixrUserSecurityDetailsService;
import com.inspaya.fixr.security.service.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
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
    private final FixrUserSecurityDetailsService detailsService;
    private final FixrUserSecurityProperties properties;

    public void addTokensToHttpResponse(FixrUserSecurityDetail securityDetail, HttpServletResponse response) throws IOException {
        UUID session = UUID.randomUUID();

        UUID accessTokenJti = JwtUtil.generateJti();
        String accessToken = jwtUtil.generateToken(securityDetail, accessTokenJti, JwtUtil.TokenType.AccessToken, session);
        ResponseCookie accessTokenCookie = createAccessToken(securityDetail.getUsername(), accessTokenJti, accessToken);

        UUID refreshTokenJti = JwtUtil.generateJti();
        String refreshToken = jwtUtil.generateToken(securityDetail, refreshTokenJti, JwtUtil.TokenType.RefreshToken, session);
        ResponseCookie refreshTokenCookie = createRefreshToken(securityDetail.getUsername(), refreshTokenJti, refreshToken);

        response.setHeader("Set-Cookie", accessTokenCookie.toString());
        response.setHeader("Set-Cookie", refreshTokenCookie.toString());
        response.getWriter().write(mapper.writeValueAsString(new JwtTokenResponseDto(
                AuthHeaderType.Bearer,
                accessToken,
                refreshToken
        )));
    }

    public void revokeTokenSession(UUID tokenSession) {
        tokenService.revoke(tokenSession);
    }

    public HttpHeaders reIssueJwtTokensCookies(String jwtToken){

        JwtTokenResponseDto jwtTokens = reIssueJwtTokens(jwtToken);

        final String accessToken = jwtTokens.accessToken();
        final HttpCookie accessTokenCookie = createAccessToken(jwtUtil.extractSubject(accessToken),
                jwtUtil.extractJti(accessToken), accessToken);

        final String refreshToken = jwtTokens.refreshToken();

        final HttpCookie refreshTokenCookie = createAccessToken(jwtUtil.extractSubject(refreshToken),
                jwtUtil.extractJti(refreshToken), refreshToken);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return headers;
    }

    public JwtTokenResponseDto reIssueJwtTokens(String jwtToken){
        if(!tokenService.isTokenValid(jwtUtil.extractJti(jwtToken), JwtUtil.TokenType.RefreshToken))
            throw new CredentialsException("Invalid credentials");

        // Load user information
        final String emailBlindIndex = jwtUtil.extractSubject(jwtToken);
        final FixrUserSecurityDetail fixrUser = detailsService.loadUserByUsername(emailBlindIndex);

        UUID session = UUID.randomUUID();
        // Generate new token pairs
        final UUID accessTokenJti = JwtUtil.generateJti();
        final String accessToken = jwtUtil.generateToken(fixrUser, accessTokenJti, JwtUtil.TokenType.AccessToken, session);

        final UUID refreshTokenJti = JwtUtil.generateJti();
        final String refreshToken = jwtUtil.generateToken(fixrUser, refreshTokenJti, JwtUtil.TokenType.RefreshToken, session);


        return new JwtTokenResponseDto(AuthHeaderType.Bearer, accessToken, refreshToken);
    }

    private ResponseCookie createAccessToken(String username, UUID jti, String token){
        var accessToken = ResponseCookie.from("AccessToken", token)
                .httpOnly(true)
                .path("/")
                .secure(properties.profile() != AppProfile.Dev)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .maxAge(Duration.ofMinutes(properties.accessTokenExpirationMinutes()))
                .build();
        UUID session = jwtUtil.extractSession(token);

        tokenService.createToken(username, jti, JwtUtil.TokenType.AccessToken, session);

        return accessToken;
    }

    private ResponseCookie createRefreshToken(String username, UUID jti, String token){
        ResponseCookie refreshTokenCookie = ResponseCookie.from("RefreshToken", token)
                .httpOnly(true)
                .secure(properties.profile() != AppProfile.Dev)
                .path("/")
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .maxAge(Duration.ofDays(properties.refreshTokenExpirationDays()))
                .build();
        UUID session = jwtUtil.extractSession(token);
        tokenService.createToken(username, jti, JwtUtil.TokenType.RefreshToken, session);

        return refreshTokenCookie;
    }

}
