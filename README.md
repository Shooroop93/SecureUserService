# SecureUserService

**SecureUserService** — это независимый микросервис для управления пользователями, их аутентификацией, авторизацией и безопасной работой с JWT-токенами. Построен на **Java + Spring Boot + gRPC**, с чистой архитектурой и упором на безопасность.

---

## 🚀 Основной функционал

- Регистрация и аутентификация пользователей (email/login + password).
- Подтверждение регистрации по email (ссылкой).
- Генерация и валидация JWT-токенов: **Access** и **Refresh**.
- Обновление токенов через `refresh_token`.
- Logout с возможностью выхода из **всех сессий** или **текущей сессии**.
- Хранение токенов в PostgreSQL и Redis.
- Управление ролями пользователей (USER, ADMIN).
- Использование Redis для кеширования токенов и временных данных.
- Управление пользователями и токенами через PostgreSQL.
- Liquibase для миграций базы данных.
- gRPC API для взаимодействия с другими микросервисами.

---

## ⚙️ Архитектура

### 1. **gRPC API**
- `AuthService`:
    - `Register` — регистрация пользователя.
    - `Login` — генерация Access/Refresh токенов.
    - `RefreshToken` — обновление Access/Refresh пары.
    - `Logout` — отзыв токенов по текущей сессии или полностью.

### 2. **REST API**
- `/api/user/confirm/{uuid}` — подтверждение регистрации через email-ссылку.

### 3. **Сервисы**
- `UsersService` — CRUD логика работы с пользователями.
- `RegistrationConfirmationService` — генерация и обработка ссылок подтверждения.
- `RedisService` — работа с Redis.
- `TokenService` — генерация, валидация, отзыв, хранение токенов.
- `LoginService` — логика аутентификации.
- `RoleService` — управление ролями (в будущем).

### 4. **Репозитории**
- `UsersRepository` — взаимодействие с таблицей пользователей.
- `TokensRepository` — работа с JWT токенами в базе.

### 5. **Модели (Entities)**
- `Users` — пользователь, флаг подтверждения, связка с токенами.
- `Tokens` — Access/Refresh токены: `revoked`, `expires_at`, `token_type`, `session_id`.

### 6. **Безопасность**
- Spring Security (частично).
- JWT токены с `jti`, `session_id`, `exp`, `role` и другими claim'ами.
- BCrypt для хеширования паролей.
- Валидация email через `EmailValidator`.
- Отзыв токенов в Redis и БД.
- Проверка просрочки и состояния `revoked`.

### 7. **Миграции**
- Liquibase: структура `db/changelog/changeset/v1/...`.
- Комментарии к таблицам, ограничения, индексы.

### 8. **Логирование**
- Логика `Slf4j`: вход/выход, ошибки, успешные события.

---

## 📄 gRPC API-эндпоинты

- `Register` — регистрация пользователя.
- `Login` — генерация Access/Refresh токенов.
- `RefreshToken` — обновление токенов.
- `Logout` — удаление токенов по сессии или всех сразу.

---

## ✅ Реализовано

- ✅ PostgreSQL + Liquibase + комментарии.
- ✅ Регистрация и подтверждение по email.
- ✅ Login (сессии, токены).
- ✅ Refresh Token (по `refresh_token`, с отзывом предыдущей сессии).
- ✅ Logout (вся сессия или только одна).
- ✅ JWT (HS512, JTI, роли, session_id, время жизни).
- ✅ Redis + PostgreSQL для хранения и отзыва.
- ✅ Юнит-тесты: `RedisService`, `UsersService`, `RegistrationConfirmationService`, `LoginService`, `TokenService`.
- ✅ gRPC API: Register, Login, RefreshToken, Logout.

---

## 🛠️ Технологии

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security**
- **gRPC**
- **JWT**
- **Redis**
- **PostgreSQL**
- **Liquibase**
- **Docker (в будущем)**

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
