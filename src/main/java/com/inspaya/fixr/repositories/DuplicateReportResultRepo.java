package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.DuplicateReportResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DuplicateReportResultRepo extends JpaRepository<DuplicateReportResult, UUID> {
    Page<DuplicateReportResult> findAllByReport_ReportIdOrderByRankAsc(UUID reportId, Pageable pageable);
    void deleteAllByReport_ReportId(UUID reportId);
}
