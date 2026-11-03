package com.inspaya.fixr.entities.token;

import com.inspaya.fixr.security.service.JwtUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "jwt_token", uniqueConstraints = {
        @UniqueConstraint(name = "jti_issued_to_unq", columnNames = {"jti", "issuedTo"}),
        @UniqueConstraint(name = "session_unq_type_issued_to", columnNames = {"issuedTo", "type", "sessionId"})
})
public class JwtToken {
    @Id
    private UUID jti;

    @JoinColumn(nullable = false, name = "issued_to")
    private String issuedTo;

    @Enumerated(EnumType.STRING)
    private JwtUtil.TokenType type;

    @Column(nullable = false)
    @Setter
    private UUID sessionId;

    @Setter
    private Boolean isUsed;
    @Setter
    private Boolean isRevoked;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public JwtToken(String user, UUID jti, JwtUtil.TokenType type){
        this.jti = jti;
        this.issuedTo = user;
        this.isUsed = false;
        this.isRevoked = false;
        this.type = type;
    }

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public void revoke(){
        this.isUsed = true;
        this.isRevoked = true;
    }
}
