package ru.etu.controlservice.exceptions;

public class JawAlreadyExistException extends RuntimeException {
    public JawAlreadyExistException(String message) {
        super(message);
    }
}
