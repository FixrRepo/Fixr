package com.inspaya.fixr.entities;

import com.inspaya.fixr.dtos.EmbeddingCandidate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "report_duplicate_unq", columnNames = {"report_id", "duplicate_report_id"}),
check = @CheckConstraint(name = "check_no_self_duplicate", constraint = "report_id <> duplicate_report_id"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class DuplicateReportResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_report_id")
    private Report duplicateReport;
    private int rank;
    private double visualScore;
    private Double distanceMeters;
    private double distanceScore;
    private double similarityScore;

    private ZonedDateTime createdAt;

    public DuplicateReportResult(Report report, Report duplicateReport, EmbeddingCandidate candidate) {
        this.report = report;
        this.duplicateReport = duplicateReport;
        this.rank = candidate.rank();
        visualScore = candidate.visualScore();
        distanceMeters = candidate.distanceMeters();
        distanceScore = candidate.distanceScore();
        similarityScore = candidate.similarityScore();
    }

    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    private void setUpdatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
}
