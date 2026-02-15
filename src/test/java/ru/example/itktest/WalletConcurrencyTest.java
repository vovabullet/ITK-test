package ru.example.itktest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.model.OperationType;
import ru.example.itktest.model.Wallet;
import ru.example.itktest.repository.WalletRepository;
import ru.example.itktest.service.WalletService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тест конкурентных операций.
 * 1000 RPS по одному кошельку.
 * Ни один запрос не должен быть не обработан (50Х error).
 */
@SpringBootTest
@Testcontainers
class WalletConcurrencyTest {
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void concurrentDeposits_shouldHandleRaceConditions() throws Exception {

        Wallet wallet = walletRepository.save(
                Wallet.builder()
                        .amount(BigDecimal.ZERO)
                        .build()
        );

        int threads = 50;
        int operations = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(operations);

        // массив для ошибок
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        // подготовка задач
        for (int i = 0; i < operations; i++) {

            executor.submit(() -> {

                try {
                    // ожидание сигнала стартовать одновременно
                    startLatch.await();

                    WalletOperationDto dto = new WalletOperationDto(
                            wallet.getId(),
                            OperationType.DEPOSIT,
                            BigDecimal.ONE
                    );

                    walletService.walletOperation(dto);

                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // одновременный запуск всех потоков
        startLatch.countDown();

        // ожидание завершения
        doneLatch.await();

        executor.shutdown();

        // Тест пройден, если:
        // - массив ошибок пуст
        assertTrue(errors.isEmpty(), "Возникшие ошибки: " + errors);

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        // - баланс на счету = 200
        assertEquals(new BigDecimal("200.00"), updated.getAmount());
    }
}
