package ru.etu.controlservice.exceptions;

public class CtAlreadyExistException extends RuntimeException {
    public CtAlreadyExistException(String message) {
        super(message);
    }
}
