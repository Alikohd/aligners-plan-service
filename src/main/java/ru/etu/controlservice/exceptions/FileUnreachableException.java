package ru.etu.controlservice.exceptions;

public class FileUnreachableException extends RuntimeException {
    public FileUnreachableException(String msg) {
        super(msg);
    }
}
