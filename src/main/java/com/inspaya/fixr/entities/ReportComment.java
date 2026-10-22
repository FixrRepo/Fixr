package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_comment", indexes = {
        @Index(name = "report_comment_report_created_idx", columnList = "report_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class ReportComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private FixrUser author;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ReportComment(Report report, FixrUser author, String content) {
        this.report = report;
        this.author = author;
        this.content = content;
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
