package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ValidityScoreEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ValidityScoreEventRepo extends JpaRepository<ValidityScoreEvent, UUID> {
    boolean existsByUserUserIdAndReportReportIdAndScoringVersion(UUID userId, UUID reportId, String scoringVersion);
    List<ValidityScoreEvent> findAllByUserUserIdOrderByCreatedAtDesc(UUID userId);
}
