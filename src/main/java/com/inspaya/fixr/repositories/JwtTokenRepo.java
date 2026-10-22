package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JwtTokenRepo extends JpaRepository<JwtToken, UUID> {

    @Query("""
            UPDATE JwtToken jt SET jt.isRevoked=true WHERE jt.issuedTo=:user AND jt.isUsed=false
            """)
    void revokeByUser(String user);

    Optional<JwtToken> findByJtiAndIsRevoked(UUID jti, boolean b);
}
