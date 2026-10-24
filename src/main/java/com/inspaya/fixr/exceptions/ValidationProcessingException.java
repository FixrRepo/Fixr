package com.inspaya.fixr.exceptions;

import java.util.UUID;

public class ValidationProcessingException extends RuntimeException {
    private final UUID outboxEventId;

    public ValidationProcessingException(UUID outboxEventId, Throwable cause) {
        super("Failed to process validation outbox event " + outboxEventId, cause);
        this.outboxEventId = outboxEventId;
    }

    public UUID getOutboxEventId() {
        return outboxEventId;
    }
}
