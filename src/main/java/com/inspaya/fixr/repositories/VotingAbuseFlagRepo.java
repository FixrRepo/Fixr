package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.VotingAbuseFlag;
import com.inspaya.fixr.enums.VotingAbusePattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VotingAbuseFlagRepo extends JpaRepository<VotingAbuseFlag, UUID> {
    boolean existsByReportReportIdAndPattern(UUID reportId, VotingAbusePattern pattern);
    List<VotingAbuseFlag> findAllByReviewedAtIsNullOrderByCreatedAtDesc();
}
