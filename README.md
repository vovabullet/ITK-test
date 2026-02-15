# Wallet Service - ITK Test Project

Тестовое задание для компании ИТК. REST API сервис для управления электронными кошельками с поддержкой высоких конкурентных нагрузок.

## Описание проекта

Приложение представляет собой REST API для управления электронными кошельками, которое:
- Принимает операции пополнения (DEPOSIT) и снятия (WITHDRAW) средств
- Позволяет получать баланс кошелька
- Обеспечивает корректную работу при высоких конкурентных нагрузках (1000 RPS на один кошелек)
- Предоставляет корректные ответы при ошибках (кошелек не найден, недостаточно средств, невалидный JSON)

## Технологический стек

- **Java**: 17
- **Spring Boot**: 3.2.5
- **Spring Data JPA**: для работы с БД
- **PostgreSQL**: 15 - основная база данных
- **Liquibase**: миграции базы данных
- **Maven**: сборка проекта
- **Docker & Docker Compose**: контейнеризация
- **Testcontainers**: для интеграционных тестов
- **Lombok**: уменьшение boilerplate кода
- **ModelMapper**: маппинг между entity и DTO

## Архитектура

Проект следует классической многоуровневой архитектуре:

```
Controller -> Service -> Repository -> Database
```

### Обработка конкурентности

Для обеспечения корректной работы при высоких конкурентных нагрузках (1000 RPS):

1. **Атомарные операции на уровне БД**: Используются атомарные SQL запросы для операций `DEPOSIT` и `WITHDRAW`:
   ```java
   UPDATE Wallet w SET w.amount = w.amount + :amount WHERE w.id = :id
   UPDATE Wallet w SET w.amount = w.amount - :amount WHERE w.id = :id AND w.amount >= :amount
   ```

2. **Транзакционность**: Все операции выполняются в транзакциях с использованием `@Transactional`

3. **Проверка на уровне БД**: Условие `WHERE w.amount >= :amount` в запросе `WITHDRAW` гарантирует, что снятие произойдет только при достаточном балансе

## API Endpoints

### 1. Создание кошелька
```http
POST /api/v1/wallet/create
```

**Ответ (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 0
}
```

### 2. Операция над кошельком
```http
POST /api/v1/wallet
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "valletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

**Примечание**: В поле `valletId` используется написание из ТЗ (с двумя 'l').

**Типы операций:**
- `DEPOSIT` - пополнение счета
- `WITHDRAW` - снятие средств

**Успешный ответ (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 1000
}
```

**Возможные ошибки:**
- `404 Not Found` - кошелек не найден
- `409 Conflict` - недостаточно средств для снятия
- `400 Bad Request` - невалидный JSON или ошибка валидации

### 3. Получение баланса кошелька
```http
GET /api/v1/wallet/{WALLET_UUID}
```

**Ответ (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 1000
}
```

**Возможные ошибки:**
- `404 Not Found` - кошелек не найден

### 4. Получение всех кошельков
```http
GET /api/v1/wallet
```

**Ответ (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 1000
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "amount": 500
  }
]
```

## Обработка ошибок

Приложение предоставляет структурированные ответы для всех ошибок:

### Кошелек не найден (404)
```json
{
  "message": "Кошелек с ID 550e8400-e29b-41d4-a716-446655440000 не найден",
  "timestamp": "2024-01-01T12:00:00"
}
```

### Недостаточно средств (409)
```json
{
  "message": "Недостаточно средств на кошельке 550e8400-e29b-41d4-a716-446655440000. Текущий баланс: 100, требуется: 1000",
  "timestamp": "2024-01-01T12:00:00"
}
```

### Невалидный запрос (400)
```json
{
  "message": "Ошибка валидации: ID кошелька обязательно",
  "timestamp": "2024-01-01T12:00:00"
}
```

## Установка и запуск

### Требования
- Docker
- Docker Compose

### Запуск с помощью Docker Compose

1. Клонируйте репозиторий:
```bash
git clone https://github.com/vovabullet/ITK-test.git
cd ITK-test
```

2. Запустите все сервисы:
```bash
docker-compose up --build
```

Приложение будет доступно по адресу: `http://localhost:8080`

PostgreSQL будет доступен на порту: `5432`

### Остановка сервисов
```bash
docker-compose down
```

### Остановка с удалением данных
```bash
docker-compose down -v
```

## Конфигурация

Приложение поддерживает конфигурацию через переменные окружения без пересборки контейнеров.

### Переменные окружения для приложения

В `docker-compose.yml` можно настроить следующие параметры:

```yaml
environment:
  # Настройки подключения к БД
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/walletdb
  SPRING_DATASOURCE_USERNAME: wallet
  SPRING_DATASOURCE_PASSWORD: wallet
  
  # Порт приложения
  SERVER_PORT: 8080
  
  # JPA настройки
  SPRING_JPA_SHOW_SQL: true
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate
```

### Переменные окружения для PostgreSQL

```yaml
environment:
  POSTGRES_DB: walletdb
  POSTGRES_USER: wallet
  POSTGRES_PASSWORD: wallet
```

### Пример изменения конфигурации

Чтобы изменить порт приложения на 9090:

```yaml
app:
  ports:
    - "9090:8080"
  environment:
    SERVER_PORT: 8080
```

Чтобы изменить параметры базы данных:

```yaml
postgres:
  environment:
    POSTGRES_DB: my_wallet_db
    POSTGRES_USER: my_user
    POSTGRES_PASSWORD: my_password
```

## Миграции базы данных

Миграции выполняются автоматически при запуске приложения с помощью Liquibase.

### Структура миграций

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml  # Главный файл миграций
└── init.yaml                 # Создание таблицы wallets
```

### Таблица wallets

```sql
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Разработка

### Локальный запуск для разработки

1. Запустите PostgreSQL:
```bash
docker-compose up postgres
```

2. Запустите приложение через Maven:
```bash
./mvnw spring-boot:run
```

### Сборка проекта

```bash
./mvnw clean package
```

### Сборка Docker образа

```bash
docker build -t wallet-service .
```

## Тестирование

Проект включает интеграционные тесты, которые покрывают все основные сценарии использования API.

### Запуск всех тестов

```bash
./mvnw test
```

### Покрытые сценарии

- ✅ Создание кошелька
- ✅ Пополнение баланса (DEPOSIT)
- ✅ Снятие средств (WITHDRAW)
- ✅ Недостаточно средств (409 Conflict)
- ✅ Кошелек не найден (404 Not Found)
- ✅ Невалидный JSON (400 Bad Request)
- ✅ Ошибки валидации (400 Bad Request)
- ✅ Конкурентные операции над одним кошельком

### Тесты конкурентности

Проект включает тесты для проверки корректной работы при высоких конкурентных нагрузках (`ConcurrencyTest.java`, `WalletConcurrencyTest.java`).

## Структура проекта

```
ITK-test/
├── src/
│   ├── main/
│   │   ├── java/ru/example/itktest/
│   │   │   ├── controller/          # REST контроллеры
│   │   │   ├── service/              # Бизнес-логика
│   │   │   ├── repository/           # Репозитории JPA
│   │   │   ├── model/                # Entity классы
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── exception/            # Обработка исключений
│   │   │   └── config/               # Конфигурация
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/         # Liquibase миграции
│   └── test/
│       └── java/ru/example/itktest/  # Тесты
├── Dockerfile                         # Сборка Docker образа
├── docker-compose.yml                 # Оркестрация контейнеров
├── pom.xml                            # Maven конфигурация
└── README.md                          # Этот файл
```

## Особенности реализации

### 1. Конкурентность
- Атомарные SQL операции на уровне базы данных
- Транзакционность всех операций
- Условная проверка баланса в SQL запросе для WITHDRAW

### 2. Обработка ошибок
- Глобальный обработчик исключений (`GlobalExceptionHandler`)
- Кастомные исключения для бизнес-логики
- Структурированные ответы об ошибках

### 3. Валидация
- Bean Validation (JSR-380) для проверки входных данных
- `@NotNull`, `@PositiveOrZero` аннотации
- Автоматическая валидация через `@Valid`

### 4. Тестирование
- Использование Testcontainers для реальной PostgreSQL в тестах
- Полное покрытие API endpoints
- Тесты конкурентности

## API Примеры использования

### Пример полного цикла работы

1. Создать кошелек:
```bash
curl -X POST http://localhost:8080/api/v1/wallet/create
```

2. Пополнить баланс:
```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "valletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "DEPOSIT",
    "amount": 1000
  }'
```

3. Снять средства:
```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "valletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "WITHDRAW",
    "amount": 500
  }'
```

4. Проверить баланс:
```bash
curl http://localhost:8080/api/v1/wallet/550e8400-e29b-41d4-a716-446655440000
```

## Health Check

Для проверки работоспособности приложения:
```bash
curl http://localhost:8080/api/v1/ping
```

## Лицензия

Этот проект создан в качестве тестового задания для компании ИТК.

## Автор

vovabullet
