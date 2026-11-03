package com.inspaya.fixr.exceptions;

public class FixrServerError extends RuntimeException {
    public FixrServerError(String message) {
        super(message);
    }
}
