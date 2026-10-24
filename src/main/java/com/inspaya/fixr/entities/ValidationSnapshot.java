package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ReportValidationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "validation_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class ValidationSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private int eligibleVoteCount;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal upvoteWeight;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal downvoteWeight;

    @Column(precision = 7, scale = 6)
    private BigDecimal supportRatio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportValidationStatus status;

    @Column(nullable = false, columnDefinition = "text")
    private String voteSnapshot;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ValidationSnapshot(Report report, int eligibleVoteCount, BigDecimal upvoteWeight,
                              BigDecimal downvoteWeight, BigDecimal supportRatio,
                              ReportValidationStatus status, String voteSnapshot) {
        this.report = report;
        this.eligibleVoteCount = eligibleVoteCount;
        this.upvoteWeight = upvoteWeight;
        this.downvoteWeight = downvoteWeight;
        this.supportRatio = supportRatio;
        this.status = status;
        this.voteSnapshot = voteSnapshot;
    }
}
