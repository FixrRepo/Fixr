package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.entities.report.Report;
import com.inspaya.fixr.entities.report.ReportFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReportFollowRepo extends JpaRepository<ReportFollow, String> {
    Optional<ReportFollow> findByReportAndFollower(Report report, FixrUser fixrUser);

    @Transactional
    void deleteByReportAndFollower(Report report, FixrUser fixrUser);
}
