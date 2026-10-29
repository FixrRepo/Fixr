package com.inspaya.fixr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardWarmup {
    private final LeaderboardProjectionService projectionService;

    @EventListener(ApplicationReadyEvent.class)
    public void enqueueExistingUsers() {
        queueRefresh();
    }

    @Scheduled(fixedDelayString = "${fixr.leaderboard.reconcile-interval-ms:3600000}",
            initialDelayString = "${fixr.leaderboard.reconcile-initial-delay-ms:3600000}")
    public void reconcileExistingUsers() {
        queueRefresh();
    }

    private void queueRefresh() {
        try {
            projectionService.enqueueAllExistingUsers();
        } catch (Exception exception) {
            log.error("Could not queue existing accounts for leaderboard projection", exception);
        }
    }
}
