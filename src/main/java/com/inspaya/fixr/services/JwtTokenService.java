package com.inspaya.fixr.services;

import com.inspaya.fixr.entities.JwtToken;
import com.inspaya.fixr.repositories.JwtTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenRepo repo;

    public void createToken(String issueTo, UUID jti){
        JwtToken token = new JwtToken(issueTo, jti);
        repo.save(token);
    }

    public Boolean isTokenValid(UUID jti){
        JwtToken token = repo.findByJtiAndIsRevoked(jti, false).orElseThrow();
        if(!token.getIsUsed()){
            token.setIsUsed(true);
            repo.save(token);
            return true;
        }
        repo.revokeByUser(token.getIssuedTo());
        // Notify user of the event
        return false;
    }
}
