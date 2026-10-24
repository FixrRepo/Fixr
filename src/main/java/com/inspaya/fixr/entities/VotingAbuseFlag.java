package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.VotingAbusePattern;
import com.inspaya.fixr.enums.AbuseReviewOutcome;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voting_abuse_flag", uniqueConstraints = @UniqueConstraint(
        name = "uk_voting_abuse_flag_report_pattern", columnNames = {"report_id", "pattern"}
))
@Getter
@Setter
@NoArgsConstructor
public class VotingAbuseFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flagged_user_id")
    private FixrUser flaggedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VotingAbusePattern pattern;

    @Column(nullable = false, columnDefinition = "text")
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private FixrUser reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AbuseReviewOutcome reviewOutcome;

    @Column(columnDefinition = "text")
    private String reviewNotes;

    public VotingAbuseFlag(Report report, FixrUser flaggedUser, VotingAbusePattern pattern, String details) {
        this.report = report;
        this.flaggedUser = flaggedUser;
        this.pattern = pattern;
        this.details = details;
    }
}
