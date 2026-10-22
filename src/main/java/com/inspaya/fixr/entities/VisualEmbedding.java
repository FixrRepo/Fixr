package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.math.Vector2D;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VisualEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_embedding_id", nullable = false)
    private ReportEmbedding reportEmbedding;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "image_embedding", nullable = false)
    @Array(length = 512)
    private double[] embedding;

    @Transient
    private double[] similarity;

    private ZonedDateTime createdAt;

    public VisualEmbedding(ReportEmbedding reportEmbedding, double[] embedding){
        this.embedding = embedding;
        this.reportEmbedding = reportEmbedding;
    }

    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    public void computeSimilarity(double[] vector){
        Vector2D vector2D = new Vector2D();

    }
}
