package ru.example.itktest.service;

import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.example.itktest.dto.WalletBalanceDto;
import ru.example.itktest.dto.WalletDto;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.exception.InsufficientFundsException;
import ru.example.itktest.exception.WalletNotFoundException;
import ru.example.itktest.model.OperationType;
import ru.example.itktest.model.Wallet;
import ru.example.itktest.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.example.itktest.model.OperationType.WITHDRAW;

/**
 * Сервис для манипуляции электронным кошельком
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;

    /**
     * Создание нового кошелька
     * @return созданный кошелек
     */
    @Transactional
    public WalletDto createWallet() {
        log.debug("Создание нового кошелька");

        Wallet wallet = new Wallet();
        wallet.setAmount(new BigDecimal("0"));

        Wallet savedWallet = walletRepository.save(wallet);

        log.info("Кошелек успешно создан с ID: {}", savedWallet.getId());
        return modelMapper.map(savedWallet, WalletDto.class);
    }

    /**
     * Проведение операции над кошельком
     * @param dto с типом операции и вносимой/снимаемой суммой
     * @return обновленный кошелек
     */
    @Transactional
    @Retryable(OptimisticLockException.class) // для обработки OptimisticLockException из Hibernate
    public WalletDto walletOperation(WalletOperationDto dto) {
        UUID id = dto.getId();
        log.debug("Проведение операции над кошельком с ID: {}", id);

        Wallet wallet = walletRepository.findById(id).
                orElseThrow(() -> new WalletNotFoundException(id));

        applyOperation(wallet, dto.getType(), dto.getAmount());

        log.debug("Операция над кошельком с ID {} успешно проведена", id);
        return modelMapper.map(wallet, WalletDto.class);
    }

    /**
     * Получение баланса кошелька
     * @param id кошелька
     * @return WalletBalanceDto, содержащий данные о балансе
     */
    @Transactional(readOnly = true)
    public WalletBalanceDto getBalance(UUID id) {
        log.debug("Получение баланса кошелька с ID: {}", id);
        Wallet wallet = walletRepository.findById(id).
                orElseThrow(() -> new WalletNotFoundException(id));

        log.debug("Баланс кошелька с ID {} успешно получен", id);
        return modelMapper.map(wallet, WalletBalanceDto.class);
    }

    private void applyOperation(Wallet wallet, OperationType type, BigDecimal amount) {
        if (type == WITHDRAW && wallet.getAmount().compareTo(amount) < 0) {
            // Недостаточно средств на счете
            throw new InsufficientFundsException(wallet.getId(), wallet.getAmount(), amount);
        }

        BigDecimal newAmount = switch(type) {
            case DEPOSIT -> wallet.getAmount().add(amount);
            case WITHDRAW -> wallet.getAmount().subtract(amount);
        };

        wallet.setAmount(newAmount);
    }
}
