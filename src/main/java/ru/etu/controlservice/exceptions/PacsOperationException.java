package ru.etu.controlservice.exceptions;

public class PacsOperationException extends RuntimeException {

    public PacsOperationException(String message, Throwable cause) {super(message, cause);}

    public PacsOperationException(String message) {super(message);}

}
