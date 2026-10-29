package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportVote;
import com.inspaya.fixr.enums.ReportVoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.ZonedDateTime;

public interface ReportVoteRepo extends JpaRepository<ReportVote, UUID> {
    Optional<ReportVote> findByReportReportIdAndUserUserId(UUID reportId, UUID userId);
    List<ReportVote> findAllByReportReportId(UUID reportId);
    List<ReportVote> findAllByUserUserId(UUID userId);
    @Query("""
            select count(distinct v.report.reportId) from ReportVote v
            where v.user.userId = :voterId and v.report.reporter.userId = :ownerId
              and coalesce(v.updatedAt, v.createdAt) >= :since
            """)
    long countDistinctReportsByVoterAndReportOwnerSince(@Param("voterId") UUID voterId,
                                                        @Param("ownerId") UUID ownerId,
                                                        @Param("since") ZonedDateTime since);
    List<ReportVote> findAllByUserUserIdAndReportReportIdIn(UUID userId, List<UUID> reportIds);
    long countByReport_ReportIdAndVoteType(UUID reportId, ReportVoteType voteType);

    long countByUserUserIdAndVoteType(UUID userId, ReportVoteType voteType);

    @Query("""
            select v.report.reportId as reportId, v.voteType as voteType, count(v) as voteCount
            from ReportVote v where v.report.reportId in :reportIds
            group by v.report.reportId, v.voteType
            """)
    List<ReportVoteCount> countVotesByReportIds(@Param("reportIds") List<UUID> reportIds);
}
