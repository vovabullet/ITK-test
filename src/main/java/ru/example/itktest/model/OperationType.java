package ru.example.itktest.model;

import lombok.Getter;

@Getter
public enum OperationType {
    DEPOSIT("Внести"),
    WITHDRAW("Вывести");

    /**
     * Отображаемый тип операции
     */
    private final String displayName;

    /**
     * Конструктор типа операции
     * @param displayName отображаемое название операции
     */
    OperationType(String displayName) {
        this.displayName = displayName;
    }

}
