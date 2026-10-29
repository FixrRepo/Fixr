package com.inspaya.fixr.services;

import com.inspaya.fixr.entities.LeaderboardOutboxEvent;
import com.inspaya.fixr.repositories.LeaderboardOutboxRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardOutboxProcessor {
    private final LeaderboardOutboxRepo outboxRepo;
    private final LeaderboardProjectionService projectionService;

    @Scheduled(fixedDelayString = "${fixr.leaderboard.outbox-poll-interval-ms:5000}")
    @Transactional
    public void processOne() {
        List<LeaderboardOutboxEvent> pending = outboxRepo.findPendingForUpdate(Instant.now(), PageRequest.of(0, 25));
        if (pending.isEmpty()) return;

        for (LeaderboardOutboxEvent event : pending) {
            try {
                projectionService.refreshUser(event.getUserId());
                event.setProcessedAt(Instant.now());
                event.setLastError(null);
            } catch (Exception exception) {
                int attempts = event.getAttempts() + 1;
                event.setAttempts(attempts);
                event.setNextAttemptAt(Instant.now().plusSeconds(Math.min(3600L, 1L << Math.min(attempts, 12))));
                String error = exception.getMessage();
                event.setLastError(error == null ? "Unknown Redis projection error"
                        : error.substring(0, Math.min(error.length(), 2000)));
                log.error("Leaderboard projection failed; retry scheduled for user {}", event.getUserId(), exception);
            }
        }
    }
}
