package ru.example.itktest.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.example.itktest.dto.WalletBalanceDto;
import ru.example.itktest.dto.WalletDto;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.exception.InsufficientFundsException;
import ru.example.itktest.exception.WalletNotFoundException;
import ru.example.itktest.model.Wallet;
import ru.example.itktest.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public WalletDto walletOperation(WalletOperationDto dto) {
        UUID id = dto.getId();
        log.debug("Проведение операции над кошельком с ID: {}", id);

        BigDecimal amount = dto.getAmount();
        int updated;

        switch (dto.getType()) {
            case WITHDRAW:
                updated = walletRepository.withdraw(id, amount);

                if (updated == 0) {
                    // wallet отсутствует или недостаточно средств
                    Wallet wallet = walletRepository.findById(id).orElseThrow(() -> new WalletNotFoundException(id));
                    throw new InsufficientFundsException(id, wallet.getAmount(), amount);
                }
                break;
            case DEPOSIT:
                updated = walletRepository.deposit(id, dto.getAmount());

                if (updated == 0) {
                    throw new WalletNotFoundException(id);
                }
                break;
        }

        Wallet wallet = walletRepository.findById(id).orElseThrow(() -> new WalletNotFoundException(id));

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

        return modelMapper.map(wallet, WalletBalanceDto.class);
    }

    /**
     * Получение всех кошельков
     * @return список всех кошельков
     */
    @Transactional(readOnly = true)
    public List<WalletDto> getAll() {
        log.debug("Получение всех кошельков");
        List<Wallet> wallets = walletRepository.findAll();

        return wallets.stream().map(wallet ->
                modelMapper.map(wallet, WalletDto.class)).
                collect(Collectors.toList());
    }
}
