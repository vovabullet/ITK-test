package ru.example.itktest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDto {
    @NotNull(message = "ID кошелька обязательно")
    private UUID id;
}
