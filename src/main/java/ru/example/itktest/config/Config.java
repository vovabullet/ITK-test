package ru.example.itktest.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Конфигурация ModelMapper для маппинга между сущностями и DTO
 */
@Configuration
@EnableRetry
public class Config {
    /**
     * Создание и настройка экземпляра ModelMapper
     * @return настроенный ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
