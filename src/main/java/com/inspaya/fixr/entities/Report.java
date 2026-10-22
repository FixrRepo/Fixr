package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.ReportStatus;
import com.inspaya.fixr.dtos.CreateReportDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("status <> 'Deleted'")
public class Report {
    @Id
    private UUID reportId;

    private String description;

    // Having to options to opt out of anonymity permanently compromises user's identity
    private Boolean isAnonymous;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    @ManyToOne
    @JoinColumn(nullable = false, name = "reporter_id")
    private FixrUser reporter;

    private Double credibilityScore;

    @ManyToOne
    private State state;
    @ManyToOne
    private Lga lga;
    @ManyToOne
    private City city;

    @Transient
    private String distance;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "report_embedding_id", nullable = false)
    private ReportEmbedding reportEmbedding;

    @OneToMany(orphanRemoval = true, mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportMedia> reportMedia;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private List<DuplicateReportResult> duplicateReportResults;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportVote> votes = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_category")
    private ReportCategory category;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public Report(CreateReportDto reportDto, UUID reportId, FixrUser reporter){
        this.reportId = reportId;
        description = reportDto.description();
        this.reporter = reporter;
        isAnonymous = reportDto.isAnonymous() == null || reportDto.isAnonymous();
        status = ReportStatus.Submitted;
        reportMedia = reportDto.mediaList().stream()
                .map((media->new ReportMedia(media, this))).toList();
        reportEmbedding = new ReportEmbedding(reportId, coordinate);
    }

}
