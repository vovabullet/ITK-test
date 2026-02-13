package ru.example.itktest.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException() {
        super("На счету недостаточно средств");
    }

    public InsufficientFundsException(UUID walletId,
                                      BigDecimal balance,
                                      BigDecimal requested) {
        super("Недостаточно средств. walletId=" + walletId +
                ", balance=" + balance +
                ", requested=" + requested);
    }
}
