package ru.example.itktest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Контроллер для проверки работоспособности сервера
 * Предоставляет эндпоинт для проверки доступности
 */
@RestController
@RequestMapping("/api")
public class PingController {

    /**
     * Проверка состояния сервера
     * @return статус сервера
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Server is running"));
    }
}
