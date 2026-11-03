package com.inspaya.fixr.controllers;

import com.inspaya.fixr.exceptions.EntityNotFoundException;
import com.inspaya.fixr.exceptions.InvalidTokenException;
import com.inspaya.fixr.exceptions.ParsingException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
@Slf4j
public class ControllerAdvice {
    public record Message(String content){}
    @ExceptionHandler(exception = {EntityNotFoundException.class, IllegalArgumentException.class, InvalidTokenException.class})
    public ResponseEntity<Message> handleEntityNotFoundException(RuntimeException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getLocalizedMessage()));
    }

    @ExceptionHandler(exception = ResponseStatusException.class)
    public ResponseEntity<Message> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new Message(e.getReason()));
    }

    @ExceptionHandler(exception = {BindException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Message> handleInvalidRequest(Exception e) {
        return ResponseEntity.badRequest().body(new Message("Invalid request"));
    }

    @ExceptionHandler(exception = ParsingException.class)
    public ResponseEntity<Message> handleParsingException(ParsingException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(e.getLocalizedMessage()));
    }

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<Message> handleException(Exception e){
        log.error("Unhandled request failure", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Message("Internal server error"));
    }
    @ExceptionHandler(exception = LockedException.class)
    public ResponseEntity<Message> handleLockedException(Exception e){
        return ResponseEntity.status(HttpStatus.LOCKED).body(new Message("Email has not been verified"));
    }
}
