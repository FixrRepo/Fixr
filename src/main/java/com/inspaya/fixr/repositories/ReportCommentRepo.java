package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportComment;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportCommentRepo extends JpaRepository<ReportComment, UUID> {
    Page<ReportComment> findAllByReport_ReportId(UUID reportId, Pageable pageable);
    Optional<ReportComment> findByCommentIdAndReport_ReportId(UUID commentId, UUID reportId);

    Optional<ReportComment> findByIdempotencyKey(@NotBlank String s);
}
