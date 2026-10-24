package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ValidationOutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.time.Instant;

public interface ValidationOutboxRepo extends JpaRepository<ValidationOutboxEvent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ValidationOutboxEvent e where e.processedAt is null "
            + "and (e.nextAttemptAt is null or e.nextAttemptAt <= :now) order by e.createdAt asc")
    java.util.List<ValidationOutboxEvent> findPendingForUpdate(
            @org.springframework.data.repository.query.Param("now") Instant now,
            org.springframework.data.domain.Pageable pageable);
}
