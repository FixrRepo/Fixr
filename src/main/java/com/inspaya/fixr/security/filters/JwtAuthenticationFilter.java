package com.inspaya.fixr.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspaya.fixr.encryption.BlindIndexService;
import com.inspaya.fixr.dtos.UserAuthDto;
import com.inspaya.fixr.exceptions.ParsingException;
import com.inspaya.fixr.security.details.FixrUserSecurityDetail;
import com.inspaya.fixr.services.JwtTokenIssueManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper mapper;
    private final AuthenticationProvider authenticationProvider;
    private final JwtTokenIssueManager manager;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("Attempting authentication for "+ request.getServletPath());
        try {
            UserAuthDto userAuthDto = mapper.readValue(request.getInputStream(), UserAuthDto.class);
            String blindIndex = BlindIndexService.generateBlindIndex(userAuthDto.username());
            var token = new UsernamePasswordAuthenticationToken(blindIndex, userAuthDto.password());
            return Objects.requireNonNull(authenticationProvider.authenticate(token));
        } catch (IOException e) {
            log.error("Authentication failed because {}", e.getLocalizedMessage());
            throw new ParsingException(e.getLocalizedMessage());
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid credentials");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        FixrUserSecurityDetail detail = (FixrUserSecurityDetail) authResult.getPrincipal();
        manager.addTokensToHttpResponse(detail, response);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().flush();
    }
}
