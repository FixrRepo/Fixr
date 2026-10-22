package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ReportVoteType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_vote", uniqueConstraints = @UniqueConstraint(
        name = "report_vote_report_user_unq", columnNames = {"report_id", "user_id"}
))
@Getter
@Setter
@NoArgsConstructor
public class ReportVote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private FixrUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportVoteType voteType;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ReportVote(Report report, FixrUser user, ReportVoteType voteType) {
        this.report = report;
        this.user = user;
        this.voteType = voteType;
    }

    @PrePersist
    private void setCreatedAt() {
        createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt() {
        updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
}
