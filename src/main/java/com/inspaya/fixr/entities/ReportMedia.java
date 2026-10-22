package com.inspaya.fixr.entities;

import com.inspaya.fixr.enums.MediaType;
import com.inspaya.fixr.dtos.MediaDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ReportMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID mediaId;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String url;

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

    public ReportMedia(MediaDto mediaDto, Report report){
        url = mediaDto.mediaUrl();
        type = mediaDto.mediaType();
        this.report = report;
    }
}
