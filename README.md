# Personal Finance — backend MVP

Backend для учета личных финансов на Java 21, Spring Boot, Maven и PostgreSQL.
Monorepo содержит модуль `backend`, Java-пакеты организованы по функциональности.

## Возможности

- Регистрация и вход по email и паролю, JWT access token на один час, BCrypt для паролей.
- Добавление доходов и расходов с суммой, категорией, датой и необязательным описанием.
- Просмотр своих операций с пагинацией и фильтрами по датам, типу и категории.
- Предустановленные категории доходов и расходов.
- Баланс: сумма всех сохраненных доходов минус сумма всех сохраненных расходов, начальное значение — 0.
- Общая сумма расходов и распределение по категориям за период: сумма и процент для каждой категории.
- OpenAPI, Swagger UI, миграции Flyway, Docker Compose.

В MVP используется только RUB. Баланс включает все внесенные операции, в том числе с будущей датой,
если пользователь внес такую операцию. Плановые операции отдельно не поддерживаются.
Email приводится к нижнему регистру. Пароль — от 8 символов и не более 72 байт UTF-8.
Пользователь определяется из JWT; передать чужой userId через API нельзя.

## Запуск через Docker Compose

Требуется Docker с запущенным Linux Engine / Docker Desktop.

1. Скопировать `.env.example` в `.env`:

   ```powershell
   Copy-Item .env.example .env
   ```

2. В `.env` задать собственный `POSTGRES_PASSWORD` и случайный `JWT_SECRET` не короче 32 байт.
   Секрет можно сгенерировать в PowerShell:

   ```powershell
   $secretBytes = New-Object byte[] 48
   $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $generator.GetBytes($secretBytes)
   [Convert]::ToBase64String($secretBytes)
   $generator.Dispose()
   ```

3. Выполнить из корня репозитория:

   ```shell
   docker compose up --build -d
   ```

API: http://localhost:8080/api/v1

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

Проверка готовности, включая соединение с БД: http://localhost:8080/actuator/health

Миграции применяются автоматически. PostgreSQL хранит данные в Docker volume `postgres_data`;
порт БД наружу не публикуется. Для остановки с сохранением данных: `docker compose down`.
Логи приложения: `docker compose logs -f backend`.

## Проверка через Swagger UI

1. Выполнить `POST /api/v1/auth/register` с email и паролем.
2. Скопировать `accessToken` из ответа, нажать **Authorize** и вставить токен без префикса `Bearer`.
3. Получить категории через `GET /api/v1/categories`.
4. Добавить доход и расход через `POST /api/v1/operations`.
5. Проверить список операций, баланс и аналитику.

Пример расхода:

```json
{
  "amount": 1250.50,
  "type": "EXPENSE",
  "categoryId": 1,
  "operationDate": "2026-11-27",
  "description": "Покупка продуктов"
}
```

Для дохода использовать `type: INCOME` и категорию доходов, например `categoryId: 7` (зарплата).

## API

| Метод | Адрес | Назначение |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Регистрация и получение токена, HTTP 201 |
| POST | `/api/v1/auth/login` | Вход и получение токена |
| GET | `/api/v1/categories?type=EXPENSE` | Категории; фильтр типа необязателен |
| POST | `/api/v1/operations` | Добавление операции, HTTP 201 |
| GET | `/api/v1/operations` | Список своих операций |
| GET | `/api/v1/balance` | Баланс, суммарные доходы и расходы |
| GET | `/api/v1/analytics/expenses` | Расходы по категориям |

Все бизнес-методы, кроме регистрации и входа, требуют `Authorization: Bearer <accessToken>`.
Сумма операции положительная, максимум 17 цифр до десятичной точки и 2 после.
Категория должна соответствовать типу операции. Дата обязательна в формате `YYYY-MM-DD`.

Список операций принимает необязательные `dateFrom`, `dateTo`, `type`, `categoryId`,
а также `page` (от 0, по умолчанию 0) и `size` (1–100, по умолчанию 20).
Сортировка: дата операции, время создания и идентификатор по убыванию.
Ответ содержит `items`, `page`, `size`, `totalElements`, `totalPages`.

Аналитика принимает `dateFrom` и `dateTo`. Без них используется текущий календарный месяц
в часовом поясе `Europe/Moscow`. Для произвольного периода нужны обе даты; границы включаются.
Неделю можно запросить теми же параметрами. При пустом периоде `totalExpenses = 0.00`, `categories = []`.
Список категорий отсортирован по убыванию расходов, категории без расходов исключаются.
Проценты округляются до двух знаков, поэтому их сумма может незначительно отличаться от 100%.

```json
{
  "dateFrom": "2026-11-01",
  "dateTo": "2026-11-30",
  "currency": "RUB",
  "totalExpenses": 1250.50,
  "categories": [
    {
      "categoryId": 1,
      "categoryName": "Продукты",
      "amount": 1250.50,
      "percentage": 100.00
    }
  ]
}
```

Ошибки возвращаются в формате Problem Details: некорректные данные — 400, ошибка авторизации — 401,
повторная регистрация email — 409. Для ошибок валидации тела запроса добавляется поле `errors`.

## Локальная разработка

Нужны JDK 21+, Maven 3.9+ и отдельная PostgreSQL 17+ с созданной пустой БД.
Maven запускается из корня репозитория:

```shell
mvn clean package -DskipTests
```

Перед запуском приложения задать переменные окружения. Файл `.env` автоматически читает только Compose,
при локальном запуске Java переменные нужно установить отдельно:

```powershell
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/personal_finance'
$env:POSTGRES_USER = 'finance'
$env:POSTGRES_PASSWORD = 'your-database-password'
$env:JWT_SECRET = 'your-random-secret-at-least-32-bytes-long'
mvn -pl backend spring-boot:run
```

## Структура

```text
backend/src/main/java/ru/itmo/finance/
  auth/        регистрация, вход, определение текущего пользователя
  operation/   создание и просмотр операций
  category/    предустановленные категории
  balance/     текущий баланс
  analytics/   распределение расходов
  config/      безопасность, OpenAPI, часы приложения
  common/      общие типы, диапазоны дат, обработка ошибок
backend/src/main/resources/db/migration/
```

MVP реализован одним приложением и одной БД. Более широкая архитектура из исходных документов
(Kafka, Redis, Keycloak, Kubernetes, банковские интеграции, лимиты и цели) выходит за согласованные шесть пунктов.
Графики строит будущий frontend по данным API. Тесты по договоренности пока не добавлены.
Refresh token, отзыв токенов и восстановление пароля в текущий MVP не входят.
Для размещения в интернете необходимо настроить HTTPS на reverse proxy.
