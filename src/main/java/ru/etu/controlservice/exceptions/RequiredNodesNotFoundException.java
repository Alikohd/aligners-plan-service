package ru.etu.controlservice.exceptions;

public class RequiredNodesNotFoundException extends RuntimeException {
    public RequiredNodesNotFoundException(String msg) {
        super(msg);
    }
}
