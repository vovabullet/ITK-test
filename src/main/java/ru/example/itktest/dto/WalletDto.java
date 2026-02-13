package ru.example.itktest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO кошелька
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
    @NotNull(message = "ID кошелька обязательно")
    private UUID id;

    @NotNull
    @PositiveOrZero(message = "Сумма не может быть отрицательной")
    private BigDecimal amount;
}
