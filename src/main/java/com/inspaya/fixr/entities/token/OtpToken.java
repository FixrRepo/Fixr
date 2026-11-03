package com.inspaya.fixr.entities.token;

import com.inspaya.fixr.dtos.OtpTokenResponse;
import com.inspaya.fixr.enums.DeliveryMedium;
import com.inspaya.fixr.enums.OtpTokenPurpose;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_phone_email_purpose", columnNames = {"phone", "email", "purpose"}))
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String tokenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMedium deliveryMedium;

    @Transient
    private String token;

    @Column(nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    private OtpTokenPurpose purpose;

    private String email;
    private String phone;

    @Column(nullable = false)
    private Boolean isUsed;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    @Column(nullable = false)
    private ZonedDateTime issuedAt;

    @PrePersist
    private void setIssuedAt(){
        this.issuedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
        this.expiresAt = issuedAt.plusMinutes(10L);
    }

    public void markUsed(){
        this.isUsed = true;
    }

    public boolean isValid(){
        return !isUsed && !this.expiresAt.isBefore(ZonedDateTime.now(ZoneId.of("Africa/Lagos")));
    }

    public OtpTokenResponse toTokenResponse(){
        return new OtpTokenResponse(token, expiresAt);
    }
}
