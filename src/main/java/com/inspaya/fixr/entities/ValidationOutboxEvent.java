package com.inspaya.fixr.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "validation_outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class ValidationOutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant processedAt;

    @Column(nullable = false)
    private int attempts;

    @Column
    private Instant nextAttemptAt;

    @Column(columnDefinition = "text")
    private String lastError;

    public ValidationOutboxEvent(UUID reportId) {
        this.reportId = reportId;
    }
}
