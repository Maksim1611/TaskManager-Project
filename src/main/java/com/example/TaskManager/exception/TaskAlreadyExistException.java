package com.example.TaskManager.exception;

public class TaskAlreadyExistException extends RuntimeException {
    public TaskAlreadyExistException(String message) {
        super(message);
    }

    public TaskAlreadyExistException() {}
}
