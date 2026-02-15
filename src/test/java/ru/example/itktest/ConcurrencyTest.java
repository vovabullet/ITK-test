package ru.example.itktest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.model.OperationType;
import ru.example.itktest.model.Wallet;
import ru.example.itktest.repository.WalletRepository;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тест конкурентных операций.
 * 1000 RPS по одному кошельку.
 * Ни один запрос не должен быть не обработан (50Х error).
 */
@Deprecated
@SpringBootTest
@AutoConfigureMockMvc
public class ConcurrencyTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 50 потоков выполнят 200 операций на внесение депозита суммой 1.
     * В конце теста идет проверка, ровняется ли баланс на счету 200.
     * Если нет, то один/несколько потоков не выполнили запрос.
     */
    @Test
    void concurrentDeposits_shouldWorkCorrectly() throws Exception {
        // массив для ошибок
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        Wallet wallet = walletRepository.save(
                Wallet.builder().amount(BigDecimal.ZERO).build());

        int threads = 50;
        int operations = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch latch = new CountDownLatch(operations);

        for (int i = 0; i < operations; i++) {

            executor.submit(() -> {
                try {

                    WalletOperationDto dto = new WalletOperationDto(
                            wallet.getId(),
                            OperationType.DEPOSIT,
                            BigDecimal.ONE);

                    mockMvc.perform(post("/api/v1/wallet")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto)))
                            .andExpect(status().isOk());

                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();

        // Тест пройден, если:
        // массив ошибок пуст
        assertTrue(errors.isEmpty());
        // баланс на счету = 200
        assertEquals(new BigDecimal("200"), updated.getAmount());
    }
}
