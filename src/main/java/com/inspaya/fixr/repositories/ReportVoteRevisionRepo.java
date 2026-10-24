package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportVoteRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportVoteRevisionRepo extends JpaRepository<ReportVoteRevision, UUID> {
}
