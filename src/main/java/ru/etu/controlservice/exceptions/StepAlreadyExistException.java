package ru.etu.controlservice.exceptions;

public class StepAlreadyExistException extends RuntimeException {
    public StepAlreadyExistException(String message) {
        super(message);
    }
}
