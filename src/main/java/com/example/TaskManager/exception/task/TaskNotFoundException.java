package com.example.TaskManager.exception.task;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }

  public TaskNotFoundException() {
  }
}
