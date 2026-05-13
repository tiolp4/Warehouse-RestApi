# Warehouse REST API

Единый бэкенд для desktop-клиентов (`warehouse-desktop`, `warehouse-logistics`) и будущего Android-приложения. Spring Boot 3.3 + JPA + Spring Security (JWT) + PostgreSQL.

## Стек

- Java 21, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL JDBC, HikariCP (через Spring Boot)
- JJWT 0.12.x (JWT)
- springdoc-openapi (Swagger UI на `/api/swagger`)
- Maven, Docker, docker-compose

## База данных

Использует существующую БД `warehouse` (таблицы `users`, `suppliers`, `products`, `warehouse_cells`, `stock`, `invoices`, `invoice_items`, `transit_shipments`, `logistics_schedules`).

JPA настроен на `ddl-auto: validate` — схему не меняет, только сверяет. Миграции (`migration_logistics.sql` из `warehouse-logistics`) применяются отдельно.

## REST контракт

Base path: `http://host:8080/api` · все эндпоинты, кроме `/v1/auth/login` и `/actuator/health`, требуют заголовок `Authorization: Bearer <jwt>`.

### Auth
| Метод | Путь            | Описание                          |
|-------|-----------------|-----------------------------------|
| POST  | `/v1/auth/login` | `{username,password}` → `{token, expiresAt, user}` |

### Catalog (любая роль)
| Метод | Путь            |
|-------|-----------------|
| GET   | `/v1/suppliers` |
| GET   | `/v1/products`  |
| GET   | `/v1/stock`     |

### Invoices
| Метод | Путь                          | Доступ      |
|-------|-------------------------------|-------------|
| GET   | `/v1/invoices`                | any         |
| GET   | `/v1/invoices/{id}/items`     | any         |
| POST  | `/v1/invoices`                | MANAGER     |
| POST  | `/v1/invoices/{id}/items`     | any         |
| POST  | `/v1/invoices/{id}/confirm`   | MANAGER     |

### Logistics (только MANAGER)
| Метод | Путь                                  |
|-------|---------------------------------------|
| GET   | `/v1/shipments`                       |
| POST  | `/v1/shipments`                       |
| PATCH | `/v1/shipments/{id}/status`           |
| PATCH | `/v1/shipments/{id}/arrived`          |
| DELETE| `/v1/shipments/{id}`                  |
| GET   | `/v1/schedules?from=&to=`             |
| POST  | `/v1/schedules`                       |
| PATCH | `/v1/schedules/{id}/status`           |
| DELETE| `/v1/schedules/{id}`                  |

### Analytics
| Метод | Путь                                  |
|-------|---------------------------------------|
| GET   | `/v1/analytics/kpi`                   |
| GET   | `/v1/analytics/invoice-flow?days=14`  |
| GET   | `/v1/analytics/transit-status`        |
| GET   | `/v1/analytics/schedule-status`       |
| GET   | `/v1/analytics/top-carriers?limit=5`  |
| GET   | `/v1/analytics/top-drivers?limit=5`   |

Swagger UI: `http://host:8080/api/swagger`

## Известная проблема Windows + AF_UNIX

На некоторых установках Windows JDK 21+ не может открыть `Selector` (Tomcat/HttpClient падают с `Unable to establish loopback connection` → `UnixDomainSockets.connect0`). Обходной путь — указать короткий путь для AF_UNIX-сокетов:

```
java -Djdk.net.unixdomain.tmpdir=C:\temp -Djava.io.tmpdir=C:\temp -jar target/warehouse-api-1.0.0.jar
```

(Каталог `C:\temp` должен существовать.)

## Запуск локально

Требуется JDK 21 + Maven + PostgreSQL с применённой схемой.

```
mvn spring-boot:run
```

Переменные окружения (опц.):
```
DB_URL=jdbc:postgresql://127.0.0.1:5432/warehouse
DB_USER=postgres
DB_PASSWORD=...
JWT_SECRET=...        # минимум 32 байта
JWT_TTL_MIN=480       # 8 часов
CORS_ORIGINS=*        # или https://app.example.com,https://another.com
SERVER_PORT=8080
```

## Деплой через Docker

Сборка и запуск (БД + API в одной сети):
```
docker compose up -d --build
```

Только API (если БД уже есть):
```
docker build -t warehouse-api .
docker run -d --name warehouse-api \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://your-db-host:5432/warehouse \
  -e DB_USER=postgres -e DB_PASSWORD=*** \
  -e JWT_SECRET=*** \
  warehouse-api
```

Healthcheck: `GET /api/actuator/health` (используется Docker и любыми оркестраторами k8s/ECS).

### Production-чеклист

- Сгенерировать сильный `JWT_SECRET` (≥32 байт случайных). Не использовать дефолт.
- Поднять API за обратным прокси (nginx/Traefik) с TLS.
- Ограничить `CORS_ORIGINS` доменом фронтенда (не `*`).
- Закрыть порт PostgreSQL снаружи; API ходит к БД по внутренней сети.
- Перевести `application.yml` на профиль `prod` или передать переменные окружения.

## Интеграция с десктоп-клиентами

Текущие JavaFX клиенты ходят в БД напрямую через JDBC. После миграции на API нужно:
1. Заменить `DatabaseManager` → HTTP-клиент (например, `java.net.http.HttpClient`).
2. Хранить JWT-токен после `/v1/auth/login` в `Session`, добавлять в заголовок каждого запроса.
3. DTO ответов в JSON соответствуют записям из `ru.warehouse.api.dto.Dtos` — повторить их структуру в клиенте или сгенерировать из OpenAPI.

Тот же контракт можно использовать для будущего Android-приложения (Retrofit / Ktor Client).
