package com.example.demo.exception;

public class LoggingException extends RuntimeException {
    public LoggingException(String message) {
        super(message);
    }
}