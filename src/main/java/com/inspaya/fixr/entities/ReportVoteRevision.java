package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ReportVoteType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report_vote_revision")
@Getter
@Setter
@NoArgsConstructor
public class ReportVoteRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vote_id", nullable = false)
    private UUID voteId;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportVoteType voteType;

    @Column(nullable = false, length = 16)
    private String action;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weightAtVote;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt = Instant.now();

    public ReportVoteRevision(ReportVote vote, String action) {
        this.voteId = vote.getId();
        this.reportId = vote.getReport().getReportId();
        this.userId = vote.getUser().getUserId();
        this.voteType = vote.getVoteType();
        this.weightAtVote = vote.getWeightAtVote();
        this.action = action;
    }
}
