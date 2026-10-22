package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class VideoEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_embedding_id", nullable = false)
    private ReportEmbedding reportEmbedding;

    private Integer frameIndex;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(nullable = false)
    @Array(length = 512)
    private float[] embedding;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public VideoEmbedding(ReportEmbedding reportEmbedding, float[] embedding ){
        this.reportEmbedding = reportEmbedding;
        this.embedding = embedding;
    }

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/lagos"));
    }
    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/lagos"));
    }
}
