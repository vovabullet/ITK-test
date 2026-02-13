package ru.example.itktest.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(UUID id) {
        super("Не найден кошелек с id: " + id);
    }
}
