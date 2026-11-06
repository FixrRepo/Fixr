package com.inspaya.fixr.services;

import com.inspaya.fixr.entities.token.JwtToken;
import com.inspaya.fixr.repositories.JwtTokenRepo;
import com.inspaya.fixr.security.service.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
    private final JwtTokenRepo repo;

    public void createToken(String issueTo, UUID jti, JwtUtil.TokenType type, UUID sessionId){
        JwtToken token = new JwtToken(issueTo, jti, type);
        token.setSessionId(sessionId);
        repo.save(token);
    }

    public Boolean isTokenValid(UUID jti, JwtUtil.TokenType type){
        JwtToken token = repo.findByJtiAndIsRevokedAndType(jti, false, type).orElseThrow();
        if(!token.getIsUsed()){
            token.setIsUsed(true);
            repo.save(token);
            return true;
        }
        int revoked = repo.revokeByUserAndSessionId(token.getIssuedTo(), token.getSessionId());
        log.info("{} tokens were successfully revoked for user {}", revoked, token.getIssuedTo());
        // Notify user of the event
        return false;
    }

    public void revoke(UUID sessionId){
        List<JwtToken> token = repo.findAllBySessionId(sessionId);
        token.forEach(JwtToken::revoke);
        repo.saveAll(token);
    }
}
