package ru.example.itktest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDto {
    @NotNull
    @PositiveOrZero(message = "Сумма не может быть отрицательной")
    private BigDecimal amount;
}
