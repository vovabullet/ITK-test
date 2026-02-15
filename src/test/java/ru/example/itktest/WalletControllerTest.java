package ru.example.itktest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.example.itktest.dto.WalletOperationDto;
import ru.example.itktest.model.OperationType;
import ru.example.itktest.model.Wallet;
import ru.example.itktest.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Тестирование эндпоинтов
 */
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * Создание кошелька
     */
    @Test
    void createWallet_shouldReturn201() throws Exception {

        mockMvc.perform(post("/api/v1/wallet/create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(0));
    }

    /**
     * Внесение депозита
     */
    @Test
    void deposit_shouldIncreaseBalance() throws Exception {

        Wallet wallet = walletRepository.save(
                Wallet.builder().amount(BigDecimal.ZERO).build());

        WalletOperationDto dto = new WalletOperationDto(
                wallet.getId(),
                OperationType.DEPOSIT,
                new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100));
    }

    /**
     * insufficient funds (недостаточно средств на балансе)
     */
    @Test
    void withdraw_notEnoughMoney_shouldReturn409() throws Exception {

        Wallet wallet = walletRepository.save(
                Wallet.builder().amount(new BigDecimal("10")).build());

        WalletOperationDto dto = new WalletOperationDto(
                wallet.getId(),
                OperationType.WITHDRAW,
                new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    /**
     * wallet not found (кошелек не найден)
     */
    @Test
    void walletNotFound_shouldReturn404() throws Exception {

        WalletOperationDto dto = new WalletOperationDto(
                UUID.randomUUID(),
                OperationType.DEPOSIT,
                new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    /**
     * invalid JSON (некорректный JSON)
     */
    @Test
    void invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{\"amount\":\"100\"}";
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * validation error
     */
    @Test
    void validationError_shouldReturn400() throws Exception {

        WalletOperationDto dto = new WalletOperationDto(
                null,
                OperationType.DEPOSIT,
                new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
