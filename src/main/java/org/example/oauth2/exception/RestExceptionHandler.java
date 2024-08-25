package org.example.oauth2.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);


    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getStatusCode().getReasonPhrase());
        body.put("message", ex.getResponseBodyAsString());
        body.put("path", request.getDescription(false));

        logger.error("HTTP client error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}