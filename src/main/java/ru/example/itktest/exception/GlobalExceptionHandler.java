package ru.example.itktest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработка исключений
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Кошелек не найден
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(WalletNotFoundException.class)
    public ErrorResponse handleWalletNotFound(WalletNotFoundException exception) {
        log.error(exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    // Недостаточно средств на балансе
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InsufficientFundsException.class)
    public ErrorResponse handleInsufficientFundsException(InsufficientFundsException exception) {
        log.error(exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}
