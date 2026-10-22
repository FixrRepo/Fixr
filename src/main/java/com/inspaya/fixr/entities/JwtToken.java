package com.inspaya.fixr.entities;

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
@Table(name = "jwt_token", uniqueConstraints = @UniqueConstraint(name = "jti_issued_to_unq", columnNames = {"jti", "issuedTo"}))
public class JwtToken {
    @Id
    private UUID jti;

    @JoinColumn(nullable = false, name = "issued_to")
    private String issuedTo;

    @Setter
    private Boolean isUsed;
    @Setter
    private Boolean isRevoked;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public JwtToken(String user, UUID jti){
        this.jti = jti;
        this.issuedTo = user;
        this.isUsed = false;
        this.isRevoked = false;
    }

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
}
