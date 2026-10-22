package com.inspaya.fixr.security.filters;

import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.security.details.FixrUserSecurityDetailsService;
import com.inspaya.fixr.security.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final FixrUserSecurityDetailsService detailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().contains("/auth") || request.getServletPath().contains("swagger-ui") || request.getServletPath().contains("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getCookies()== null || request.getHeader(HttpHeaders.AUTHORIZATION) == null || request.getServletPath().endsWith("/auth/login")){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            String authHeader = request.getHeader("Authorization");
            String jwtToken = parseAuthorizationHeader(authHeader);

            UsernamePasswordAuthenticationToken authToken = parseToken(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (NoSuchElementException ignored) {
            // Fallback to Authentication cookies
            try{
                Cookie[] cookies = request.getCookies();
                Cookie accessCookie = Arrays.stream(cookies)
                        .filter(cookie -> Objects.equals(cookie.getName(), "AccessToken"))
                        .findFirst().orElseThrow();
                UsernamePasswordAuthenticationToken authToken = parseToken(accessCookie.getValue());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
            } catch (Exception ex) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        }
    }

    private String parseAuthorizationHeader(String authHeader){
        if(authHeader == null || authHeader.isEmpty())
            throw new RuntimeException("Null or empty header");

        String token = authHeader.substring(7);
        if(token.isEmpty())
            throw new RuntimeException("Invalid token");

        return token;
    }
    private UsernamePasswordAuthenticationToken parseToken(String token){
        if(token == null || jwtUtil.isExpired(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        try{
            String blindEmailIndex = jwtUtil.extractSubject(token);
            FixrUserSecurityDetail userSecurityDetail = detailsService.loadUserByUsername(blindEmailIndex);
            return new UsernamePasswordAuthenticationToken(userSecurityDetail, null, userSecurityDetail.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
