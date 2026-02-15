package ru.example.itktest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.itktest.model.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO проведения операции над кошельком
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationDto {
    @JsonProperty("valletId") // имя поля в JSON
    @NotNull(message = "ID кошелька обязательно")
    private UUID id;

    @JsonProperty("operationType")
    @NotNull(message = "Статус обязателен")
    private OperationType type;

    @NotNull
    @PositiveOrZero(message = "Сумма не может быть отрицательной")
    private BigDecimal amount;
}
