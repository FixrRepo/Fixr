package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "user_validity_profile")
@Check(constraints = "validity_score >= 0 AND validity_score <= 100")
@Getter
@Setter
@NoArgsConstructor
public class UserValidityProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private FixrUser user;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal validityScore = BigDecimal.TEN;

    @Column(nullable = false)
    private int scoredAssessments;

    @Version
    private long version;

    public UserValidityProfile(FixrUser user) {
        this.user = user;
        this.validityScore = BigDecimal.TEN;
        user.setCredibilityScore(10.0);
    }
}
