package ru.etu.controlservice.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.etu.controlservice.dto.ErrorResponse;
import ru.etu.controlservice.exceptions.CaseNotFoundException;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.exceptions.PacsOperationException;
import ru.etu.controlservice.exceptions.PatientNotFoundException;
import ru.etu.controlservice.exceptions.RequiredNodesNotFoundException;
import ru.etu.controlservice.exceptions.S3OperationException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({S3OperationException.class, PacsOperationException.class, RequiredNodesNotFoundException.class,
            CaseNotFoundException.class, NodeNotFoundException.class, PatientNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(Exception ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleFallbackException(RuntimeException ex) {
        log.error("Unhandled exception", ex);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred", Instant.now());
    }
}
