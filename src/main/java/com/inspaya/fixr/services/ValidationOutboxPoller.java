package com.inspaya.fixr.services;

import com.inspaya.fixr.exceptions.ValidationProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationOutboxPoller {
    private final ValidationProcessingService processingService;

    @Scheduled(fixedDelayString = "${fixr.validation.outbox-poll-interval-ms:1000}")
    public void poll() {
        try {
            processingService.processOne();
        } catch (ValidationProcessingException exception) {
            try {
                processingService.recordFailure(exception.getOutboxEventId(), exception.getCause().getMessage());
            } catch (Exception recordException) {
                log.error("Failed to record validation retry state", recordException);
            }
            log.error("Validation outbox processing failed; retry scheduled", exception);
        } catch (Exception exception) {
            log.error("Validation outbox processing failed; the event will be retried", exception);
        }
    }
}
