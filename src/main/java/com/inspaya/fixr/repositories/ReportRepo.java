package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.Report;
import com.inspaya.fixr.entities.ReportCategory;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.entities.State;
import com.inspaya.fixr.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;

public interface ReportRepo extends JpaRepository<Report, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.reportId = :reportId")
    Optional<Report> findForValidation(@Param("reportId") UUID reportId);
    Page<Report> findAllByReporter(FixrUser user, Pageable request);

    Optional<Report> findByReportIdAndReporter(String reportId, FixrUser user);

    Page<Report> findAllByReporterAndIsAnonymousFalse(FixrUser reporter, Pageable pageable);

    @Query(
            value = """
        SELECT r
        FROM Report r
        ORDER BY distance(r.coordinate, :point) ASC
        """,
            countQuery = """
        SELECT COUNT(r)
        FROM Report r
        """
    )
    Page<Report> findReportsByLocation(
            @Param("point") Point point,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT r
        FROM Report r
        WHERE r.category = :category
        ORDER BY distance(r.coordinate, :point) ASC
        """,
            countQuery = """
        SELECT COUNT(r)
        FROM Report r
        WHERE r.category = :category
        """
    )
    Page<Report> findReportsByCategory(
            @Param("point") Point point,
            @Param("category") ReportCategory category,
            Pageable pageable
    );

    @Query(value = """
        SELECT r
        FROM Report r
        WHERE (:category IS NULL OR r.category = :category)
          AND (:status IS NULL OR r.status = :status)
          AND (:state IS NULL OR r.state = :state)
        ORDER BY distance(r.coordinate, :point) ASC
        """,
            countQuery = """
        SELECT COUNT(r)
        FROM Report r
        WHERE (:category IS NULL OR r.category = :category)
          AND (:status IS NULL OR r.status = :status)
          AND (:state IS NULL OR r.state = :state)
        """
    )
    Page<Report> findReportBySpecification(
            @Param("point") Point point,
            @Param("category") ReportCategory category,
            @Param("state") State state,
            @Param("status") ReportStatus status,
            Pageable pageable
    );


    Optional<Report> findByIdempotencyKey(@NotBlank String key);
}
