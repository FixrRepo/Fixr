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
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final FixrUserSecurityDetailsService detailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.contains("/auth/") ||
                path.contains("/swagger-ui") ||
                path.contains("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Optional<String> token = extractToken(request);

        if(token.isPresent()){
            try{
                UsernamePasswordAuthenticationToken authenticationToken = parseToken(token.get());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                 log.debug("Jwt authentication failed");
                 SecurityContextHolder.clearContext();

                 response.sendError(
                         HttpServletResponse.SC_UNAUTHORIZED,
                         "Invalid or expired token"
                 );
                 return;
            }
        }

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        log.info("Request: {}", request.getRequestURI());
        log.info("Authenticated: {}",
                authentication != null && authentication.isAuthenticated());

        log.info("Principal: {}",
                authentication != null ? authentication.getPrincipal() : null);

        log.info("Authorities: {}",
                authentication != null ? authentication.getAuthorities() : null);

        filterChain.doFilter(request, response);

    }

    private Optional<String> extractToken(HttpServletRequest request){
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authorization != null && authorization.startsWith("Bearer ")){
            return Optional.of(authorization.substring(7).trim());
        }

        Cookie[] cookies = request.getCookies();
        if(cookies == null)
            return Optional.empty();

        return Arrays.stream(cookies)
                .filter(cookie -> "AccessToken".equals(cookie.getName()))
                .map(Cookie::getName)
                .filter(v-> v!= null && !v.isBlank())
                .findFirst();
    }


    private UsernamePasswordAuthenticationToken parseToken(String token){
        if(token == null || jwtUtil.isExpired(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        try{
            String blindEmailIndex = jwtUtil.extractSubject(token);
            UUID sessionId = jwtUtil.extractSession(token);
            FixrUserSecurityDetail userSecurityDetail = detailsService.loadUserByUsername(blindEmailIndex);
            userSecurityDetail.setSessionId(sessionId);
            return new UsernamePasswordAuthenticationToken(userSecurityDetail, null, userSecurityDetail.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
