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
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тестирование эндпоинтов
 */
@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ObjectMapper objectMapper;

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

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
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
