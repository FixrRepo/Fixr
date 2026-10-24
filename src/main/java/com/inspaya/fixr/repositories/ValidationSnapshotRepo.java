package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ValidationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface ValidationSnapshotRepo extends JpaRepository<ValidationSnapshot, UUID> {
    Optional<ValidationSnapshot> findFirstByReportReportIdOrderByCreatedAtDesc(UUID reportId);
}
