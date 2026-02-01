# API Pagination & Standards — Design

## Overview

Стандартизация API: пагинация, rate limiting, общие контракты.

## Components

### 1. Pagination Contracts

```kotlin
data class PageRequest(
    val pageSize: Int = 20,
    val pageToken: String? = null,
    val sortBy: String = "createdAt",
    val sortOrder: SortOrder = SortOrder.DESC
)

data class PageResponse<T>(
    val items: List<T>,
    val nextPageToken: String?,
    val totalCount: Long
)
```

### 2. Rate Limiting Plugin

- In-memory token bucket per user/device
- Headers: X-RateLimit-Remaining, X-RateLimit-Reset
- 429 response when exceeded

### 3. Common Response Wrappers

```kotlin
@Serializable
data class ListResponse<T>(
    val items: List<T>,
    val total: Int,
    val nextPageToken: String? = null
)
```

## Rate Limits

| Endpoint Type | Limit |
|--------------|-------|
| Admin API | 100 req/min |
| Player API | 60 req/min |

## Integration

- RateLimitPlugin installed in Application.module()
- Pagination helpers used in list routes
