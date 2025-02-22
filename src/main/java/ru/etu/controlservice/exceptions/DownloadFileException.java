package ru.etu.controlservice.exceptions;

public class DownloadFileException extends RuntimeException {
    public DownloadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
