# SecureUserService

**SecureUserService** — это независимый микросервис для управления пользователями, их аутентификацией, авторизацией и выдачей JWT-токенов. Сервис разработан с использованием Java, Spring Boot и gRPC, с упором на чистую и изолированную архитектуру.

## 🚀 Основной функционал
- Регистрация и аутентификация пользователей (email/login + password).
- Подтверждение регистрации по email (ссылкой).
- Генерация и валидация JWT-токенов.
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
- `TokenService` — управление JWT-токенами (в будущем).
- `RoleService` — управление ролями (в будущем).

### 4. **Репозитории**
- `UsersRepository` — работа с таблицей пользователей в PostgreSQL.

### 5. **Модели (Entities)**
- `Users` — сущность пользователя с флагом `isVerified`.

### 6. **Безопасность**
- Spring Security (в процессе).
- JWT (в процессе).
- Пароли хешируются через `BCrypt`.
- Возможность настройки `require-verification` через `application.yaml`.

### 7. **Миграции**
- Liquibase + структуры `changelog/changeset/v1/...`.
- Комментарии к таблицам и полям.

### 8. **Логирование**
- Конфигурация `logging.level`, форматирование, трассировка изменений пользователя через `UsersListener`.

---

## 📄 gRPC API-эндпоинты

- `Register` — регистрация нового пользователя, с генерацией ссылки при включенной настройке `require-verification`.
- `Login` — (в разработке).
- `RefreshToken` — (в разработке).

---

## ✅ Реализовано на текущий момент

- ✅ База данных: PostgreSQL + Liquibase + комментарии к схемам.
- ✅ Регистрация пользователя.
- ✅ Подтверждение регистрации через ссылку (Redis + UUID).
- ✅ Юнит-тесты: `RedisService`, `UsersService`, `RegistrationConfirmationService`.
- ✅ Конфигурация поведения через `application.yaml`.
- ✅ gRPC API: реализован метод `Register`.

---

## 🛠️ Технологии
- **Java 21**
- **Spring Boot 3.x**
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
- Все токены будут храниться и проверяться (в следующих версиях).

---

## ⚙️ Конфигурация через `application.yaml`
```yaml
spring:
  datasource:
    url: url
    username: username
    password: password
  data:
    redis:
      host: localhost
      port: 6379
      password: redispass
  user:
    registration:
      require-verification: true
    confirmation-link:
      retention-time: 1440
  liquibase:
    change-log: classpath:/db/changelog/changelog-master.yaml

grpc:
  server:
    port: 9090

security:
  bcrypt:
    strength: 10
```

---