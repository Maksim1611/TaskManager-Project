package com.example.TaskManager.exception.task;

public class TaskAlreadyExistException extends RuntimeException {
    public TaskAlreadyExistException(String message) {
        super(message);
    }

    public TaskAlreadyExistException() {}
}
