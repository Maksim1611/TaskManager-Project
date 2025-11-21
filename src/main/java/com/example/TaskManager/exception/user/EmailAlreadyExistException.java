package com.example.TaskManager.exception.user;

public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException(String message) {
        super(message);
    }

    public EmailAlreadyExistException() {
    }
}
