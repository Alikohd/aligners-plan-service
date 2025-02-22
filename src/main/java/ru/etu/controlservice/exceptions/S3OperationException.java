package ru.etu.controlservice.exceptions;

public class S3OperationException extends RuntimeException {
    public S3OperationException(Throwable cause) {
        super(cause);
    }

    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
