package com.example.TaskManager.exception;

public class PreferenceNotFoundException extends RuntimeException {
    public PreferenceNotFoundException(String message) {
        super(message);
    }

    public PreferenceNotFoundException() {}
}
