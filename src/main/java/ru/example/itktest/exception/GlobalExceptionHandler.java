package ru.example.itktest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        log.error("Wallet Not Found: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    // Недостаточно средств на балансе
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InsufficientFundsException.class)
    public ErrorResponse handleInsufficientFundsException(InsufficientFundsException exception) {
        log.error("Insufficient Funds: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    // Битый json
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleBadJson(HttpMessageNotReadableException exception) {
        log.error("Invalid JSON: {}", exception.getMessage());
        return new ErrorResponse("Некорректный JSON");
    }

    // Ошибка валидации
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации");
        log.error("Validation error: {}", message);
        return new ErrorResponse(message);
    }
}
