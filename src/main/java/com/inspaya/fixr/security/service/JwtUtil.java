package com.inspaya.fixr.security.service;

import com.inspaya.fixr.security.StringList;
import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class JwtUtil {
    private final FixrUserSecurityProperties properties;
    private SecretKey key;

    public enum TokenType{
        AccessToken,
        RefreshToken
    }

    public static UUID generateJti(){
        return UUID.randomUUID();
    }

    @PostConstruct
    private void initializeVariables(){
        this.key = Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(FixrUserSecurityDetail userSecurityDetail, UUID jti, TokenType tokenType) {
        List<String> authorities = userSecurityDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        Duration duration = tokenType==TokenType.AccessToken? Duration.ofMinutes(properties.accessTokenExpirationMinutes()):
                Duration.ofDays(properties.refreshTokenExpirationDays());
        return Jwts.builder().signWith(key)
                .subject(userSecurityDetail.getUsername())
                .id(jti.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration.toMillis()))
                .issuer("Fixr")
                .claim("authorities", authorities)
                .compact();
    }

    public String extractSubject(String token){
        return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public List<String> extractAuthorities(String token){
        return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("authorities", StringList.class);
    }

    public UUID extractJti(String token){
        return UUID.fromString(Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getId());
    }

    public Boolean isExpired(String token){
        return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date(System.currentTimeMillis()));
    }
}
