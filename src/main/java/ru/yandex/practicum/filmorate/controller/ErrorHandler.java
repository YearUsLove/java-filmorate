package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleValidation(final ValidationException ex) {
        return ErrorResponse.builder()
                .error(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ErrorResponse.builder()
                .error(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        return ErrorResponse.builder()
                .error(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException ex) {
        return ErrorResponse.builder()
                .error(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse handleDataIntegrityViolation(final DataIntegrityViolationException ex) {
        return ErrorResponse.builder()
                .error(ex.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception ex) {
        return ErrorResponse.builder()
                .error(ex.getClass() + ": " + ex.getMessage() + " Stack trace: " +
                        Arrays.toString(ex.getStackTrace()) + " Caused by: " + ex.getCause())
                .build();
    }
}
