package ru.example.itktest.exception;

/**
 * DTO для возвращения сообщения ошибки
 * @param message сообщение ошибки/исключения
 */
public record ErrorResponse(String message) {}
