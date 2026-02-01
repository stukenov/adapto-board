## Context

Проект использует Ktor как HTTP-фреймворк. Сейчас нет единого формата ошибок — каждый endpoint возвращает ошибки по-разному. Это создаёт проблемы:
- Клиенты (admin web, player) не могут единообразно обрабатывать ошибки
- Сложно отлаживать проблемы без correlation ID
- Нет стабильных кодов для программной обработки

## Goals / Non-Goals

**Goals:**
- Единый формат всех API ошибок (code, message, details, requestId)
- Стабильные коды ошибок для программной обработки
- Request correlation через requestId в response и логах
- Централизованная обработка exceptions через Ktor StatusPages plugin

**Non-Goals:**
- Локализация сообщений об ошибках (будет отдельным изменением)
- Retry-логика на клиенте
- Rate limiting errors (отдельное изменение)

## Decisions

### 1. Error Envelope как data class в contracts

**Решение:** `ApiError` data class в `libs/contracts` с kotlinx.serialization

**Альтернативы:**
- Sealed class hierarchy — избыточно, все ошибки имеют одинаковую структуру
- Generic Map — теряем type safety

**Структура:**
```kotlin
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: JsonObject? = null,
    val requestId: String
)
```

### 2. Error Codes как enum в contracts

**Решение:** Enum `ErrorCode` группированный по domain

**Альтернативы:**
- String constants — нет compile-time проверки
- Sealed class — избыточно для простых кодов

### 3. Exception Hierarchy

**Решение:** `ApiException` sealed class с вариантами для каждого HTTP статуса

```kotlin
sealed class ApiException(
    val code: ErrorCode,
    override val message: String,
    val details: JsonObject? = null
) : Exception(message) {
    class BadRequest(...) : ApiException(...)
    class Unauthorized(...) : ApiException(...)
    class Forbidden(...) : ApiException(...)
    class NotFound(...) : ApiException(...)
    class Conflict(...) : ApiException(...)
    class UnprocessableEntity(...) : ApiException(...)
    class InternalError(...) : ApiException(...)
}
```

**Альтернативы:**
- Один класс с httpStatus полем — менее выразительно в коде
- Exception per error code — слишком много классов

### 4. Request ID через Ktor plugin

**Решение:** Custom Ktor plugin `RequestIdPlugin`:
- Генерирует UUID если нет `X-Request-Id` header
- Сохраняет в call attributes
- Добавляет в response header
- MDC context для логирования

**Альтернативы:**
- Interceptor — менее идиоматично для Ktor
- Middleware pattern — Ktor использует plugins

### 5. Error Handling через StatusPages

**Решение:** Ktor `StatusPages` plugin для централизованной обработки:
- Ловит `ApiException` и конвертит в `ApiError` response
- Ловит неожиданные exceptions как 500
- Всегда добавляет requestId

## Risks / Trade-offs

**[Risk] Breaking change для существующих клиентов**
→ Mitigation: Все endpoints новые, нет legacy клиентов

**[Risk] Details как JsonObject может содержать sensitive data**
→ Mitigation: Review всех мест где создаётся ApiException, не класть sensitive данные

**[Trade-off] Enum вместо string codes**
→ Менее гибко для добавления новых кодов, но compile-time safety важнее

**[Trade-off] RequestId UUID vs shorter ID**
→ UUID длиннее, но стандартный и гарантированно уникальный
