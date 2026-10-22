package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.EmbeddingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class ReportEmbedding {
    @Id
    private UUID id;

    @OneToMany(mappedBy = "reportEmbedding", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VisualEmbedding> visualEmbeddings;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    private double[] textEmbedding;
    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinate;

    @Enumerated(value = EnumType.STRING)
    private EmbeddingStatus status;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @Transient
    private double distance;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public ReportEmbedding(UUID id, Point point ){
        this.id = id;
        this.coordinate = point;
        status = EmbeddingStatus.Pending;
    }

    public void createEmbeddings( List<double[]> images, List<double[]> videos){
        if(images != null && !images.isEmpty()) {
            var imageEmbeddings = images.stream()
                    .map(embedding -> new VisualEmbedding(this, embedding)).toList();
            this.setVisualEmbeddings(imageEmbeddings);
        }

        if(videos != null && !videos.isEmpty()) {
            var videoEmbeddings = videos.stream()
                    .map(embedding -> new VisualEmbedding(this, embedding)).toList();
            this.getVisualEmbeddings().addAll(videoEmbeddings);
        }
    }
}
