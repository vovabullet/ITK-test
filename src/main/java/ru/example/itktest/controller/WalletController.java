package ru.example.itktest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.itktest.dto.WalletBalanceDto;
import ru.example.itktest.dto.WalletDto;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.service.WalletService;

import java.util.UUID;

/**
 * Контроллер для управления электронными кошельками.
 */
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    /**
     * Создание нового электронного кошелька.
     * @return созданный кошелек с присвоенным идентификатором
     */
    @PostMapping
    public ResponseEntity<WalletDto> createWallet() {
        WalletDto createdWallet = walletService.createWallet();
        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    /**
     * Проведение операции над заявкой.
     * @param walletOperationDto данные для проведения операции, содержащие тип операции и сумму денежных средств
     * @return обновленный электронный кошелек
     */
    @PatchMapping
    public ResponseEntity<WalletDto> walletOperation(@Valid @RequestBody WalletOperationDto walletOperationDto) {
        WalletDto updatedWallet = walletService.walletOperation(walletOperationDto);
        return ResponseEntity.ok(updatedWallet);
    }

    /**
     * Получение баланса электронного кошелька
     * @param WALLET_UUID кошелька
     * @return баланс электронного кошелька
     */
    @GetMapping("/{WALLET_UUID}")
    public ResponseEntity<WalletBalanceDto> getBalance(@PathVariable UUID WALLET_UUID) {
        return ResponseEntity.ok(walletService.getBalance(WALLET_UUID));
    }
}
