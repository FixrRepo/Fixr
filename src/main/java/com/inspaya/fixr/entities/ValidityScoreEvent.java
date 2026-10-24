package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ValidityScoreReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "validity_score_event", uniqueConstraints = @UniqueConstraint(
        name = "uk_score_event_user_report_version",
        columnNames = {"user_id", "report_id", "scoring_version"}
))
@Getter
@Setter
@NoArgsConstructor
public class ValidityScoreEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private FixrUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_snapshot_id", nullable = false)
    private ValidationSnapshot validationSnapshot;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreDelta;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ValidityScoreReason reason;

    @Column(nullable = false, length = 32)
    private String scoringVersion;

    @Column(nullable = false)
    private int evaluationEligibleVoters;

    @Column(precision = 7, scale = 6)
    private BigDecimal evaluationSupportRatio;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ValidityScoreEvent(FixrUser user, Report report, ValidationSnapshot snapshot,
                              BigDecimal scoreDelta, BigDecimal scoreAfter,
                              ValidityScoreReason reason, String scoringVersion,
                              int evaluationEligibleVoters, BigDecimal evaluationSupportRatio) {
        this.user = user;
        this.report = report;
        this.validationSnapshot = snapshot;
        this.scoreDelta = scoreDelta;
        this.scoreAfter = scoreAfter;
        this.reason = reason;
        this.scoringVersion = scoringVersion;
        this.evaluationEligibleVoters = evaluationEligibleVoters;
        this.evaluationSupportRatio = evaluationSupportRatio;
    }
}
