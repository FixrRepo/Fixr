package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ReportVoteType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "report_vote", uniqueConstraints = @UniqueConstraint(
        name = "report_vote_report_user_unq", columnNames = {"report_id", "user_id"}
))
@Check(constraints = "weight_at_vote >= 0 AND weight_at_vote <= 1")
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

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weightAtVote;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ReportVote(Report report, FixrUser user, ReportVoteType voteType, BigDecimal weightAtVote) {
        this.report = report;
        this.user = user;
        this.voteType = voteType;
        this.weightAtVote = weightAtVote;
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
