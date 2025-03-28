# SecureUserService

**SecureUserService** — это независимый микросервис для управления пользователями, их аутентификацией, авторизацией и выдачей JWT-токенов. Сервис разработан с использованием Java, Spring Boot и gRPC, с упором на чистую и изолированную архитектуру.

## 🚀 Основной функционал
- Регистрация и аутентификация пользователей (email/login + password).
- Подтверждение регистрации по email (ссылкой).
- Генерация и валидация JWT-токенов (access и refresh).
- Хранение токенов в PostgreSQL и Redis.
- Управление ролями пользователей (USER, ADMIN).
- Кэширование токенов и подтверждений в Redis.
- Управление данными пользователей в PostgreSQL.
- Использование Liquibase для миграций.
- gRPC API для взаимодействия.
---

## ⚙️ Архитектура

### 1. **gRPC API**
- `AuthService` — регистрация, вход, обновление токена.

### 2. **REST API**
- `/api/user/confirm/{uuid}` — подтверждение регистрации пользователя.

### 3. **Сервисы**
- `UsersService` — работа с данными пользователей.
- `RegistrationConfirmationService` — генерация и проверка ссылок подтверждения регистрации.
- `RedisService` — обёртка над Redis.
- `TokenService` — генерация, валидация и отзыв JWT-токенов.
- `LoginService` — логика входа пользователя.
- `RoleService` — управление ролями (в будущем).

### 4. **Репозитории**
- `UsersRepository` — работа с таблицей пользователей.
- `TokensRepository` — работа с таблицей токенов.

### 5. **Модели (Entities)**
- `Users` — сущность пользователя с флагом `isVerified` и связью с токенами.
- `Tokens` — сущность токена, с полями `revoked`, `expires_at`, `token_type`, `user_id`.

### 6. **Безопасность**
- Spring Security (частично).
- JWT (реализовано полностью).
- Пароли хешируются через `BCrypt`.
- Валидация почты через `EmailValidator`.
- Возможность настройки `require-verification` через `application.yaml`.

### 7. **Миграции**
- Liquibase + структуры `changelog/changeset/v1/...`.
- Комментарии к таблицам и полям.

### 8. **Логирование**
- Конфигурация `logging.level`, форматирование.
- Трассировка действий: создание, удаление, обновление.

---

## 📄 gRPC API-эндпоинты

- `Register` — регистрация нового пользователя, с генерацией ссылки при включенной настройке `require-verification`.
- `Login` — проверка логина, генерация и возврат пары access/refresh токенов.
- `RefreshToken` — (в разработке).

---

## ✅ Реализовано на текущий момент

- ✅ База данных: PostgreSQL + Liquibase + комментарии к схемам.
- ✅ Регистрация пользователя.
- ✅ Подтверждение регистрации через ссылку (Redis + UUID).
- ✅ Аутентификация с генерацией и возвратом access/refresh токенов.
- ✅ Сохранение токенов в PostgreSQL и Redis.
- ✅ Юнит-тесты: `RedisService`, `UsersService`, `RegistrationConfirmationService`, `LoginService`.
- ✅ Конфигурация поведения через `application.yaml`.
- ✅ gRPC API: реализованы `Register`, `Login`.

---

## 🛠️ Технологии
- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** (частично)
- **gRPC**
- **Redis**
- **PostgreSQL**
- **Liquibase**
- **Docker** (в будущем для деплоя)

---

## ⚡ Быстрый старт

### 1. Запуск PostgreSQL и Redis
```bash
docker-compose up -d
```

### 2. Запуск приложения
```bash
./mvnw spring-boot:run
```

### 3. Проверка gRPC API
- Использовать gRPC-клиент (например, BloomRPC, Insomnia, Postman).

### 4. Проверка подтверждения регистрации
```http
GET http://localhost:8080/api/user/confirm/{uuid}
```

---

## 🔐 Безопасность
- Пароли: `BCrypt`.
- Подтверждение регистрации: Redis-ссылка TTL.
- Токены: Access/Refresh JWT, хранятся в PostgreSQL + Redis.
- Валидация токенов и проверка `revoked`.

---

## ⚙️ Конфигурация через `application-secrets.yml`
```yaml
spring:
  datasource:
    url: url
    username: username
    password: password
  data:
    redis:
      host: host
      port: port
      username: default
      password: password

  security:
    jwt:
      secret: secret
      expiration:
        access: 0
        refresh: 0
```
---
