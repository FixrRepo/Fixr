package com.inspaya.fixr.controllers;

import com.inspaya.fixr.exceptions.EntityNotFoundException;
import com.inspaya.fixr.exceptions.ParsingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
public class ControllerAdvice {
    public record Message(String content){}
    @ExceptionHandler(exception = {EntityNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<Message> handleEntityNotFoundException(RuntimeException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getLocalizedMessage()));
    }

    @ExceptionHandler(exception = ResponseStatusException.class)
    public ResponseEntity<Message> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new Message(e.getReason()));
    }

    @ExceptionHandler(exception = ParsingException.class)
    public ResponseEntity<Message> handleParsingException(ParsingException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(e.getLocalizedMessage()));
    }

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<Message> handleException(Exception e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getLocalizedMessage()));
    }
}
