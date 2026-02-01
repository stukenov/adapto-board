# Project Setup — Design

## Context

Playout Edge — multi-app Kotlin project: server (Ktor), Android TV player, shared libs. Нужна структура, которая:
- Обеспечивает чистые границы модулей
- Позволяет шарить код между apps
- Поддерживает независимую сборку apps

## Goals / Non-Goals

**Goals:**
- Gradle multi-module с Kotlin DSL
- Shared contracts между server и player
- Docker-compose для локальной разработки
- Dependency rules enforcement

**Non-Goals:**
- Kubernetes configs (позже)
- CI/CD pipelines (отдельная задача)
- Production deployment scripts

## Decisions

### Decision 1: Module Structure

```
playout-edge/
├── apps/
│   ├── server/              # Ktor монолит
│   └── player-androidtv/    # Android TV app
├── libs/
│   ├── contracts/           # Shared DTO, API models
│   ├── domain/              # Domain model (no deps)
│   ├── persistence/         # Exposed + migrations
│   ├── auth/                # JWT, RBAC, device auth
│   ├── storage/             # LOCAL/S3 storage
│   ├── overlay/             # State+patch model
│   └── observability/       # Metrics, logging
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── docker-compose.yml
```

### Decision 2: Dependency Graph

```
                    ┌─────────────┐
                    │  contracts  │  (no deps)
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌─────▼─────┐      ┌─────▼─────┐
   │ domain  │       │   auth    │      │  storage  │
   │(no deps)│       └─────┬─────┘      └─────┬─────┘
   └────┬────┘             │                  │
        │            ┌─────▼─────┐            │
        │            │persistence│            │
        │            └─────┬─────┘            │
        │                  │                  │
   ┌────▼──────────────────▼──────────────────▼────┐
   │                    server                      │
   └───────────────────────────────────────────────┘
```

### Decision 3: Technology Stack

**Server:**
- Kotlin 2.x
- Ktor 2.x (HTTP, SSE)
- Exposed (ORM)
- Flyway (migrations)
- kotlinx.serialization
- Micrometer + Prometheus

**Player:**
- Kotlin + Coroutines
- Media3 (ExoPlayer)
- Jetpack Compose (overlay UI)
- Ktor client или OkHttp
- DataStore (config persistence)

### Decision 4: Docker Compose (Local Dev)

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: playoutedge
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: dev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: ./apps/server
    depends_on:
      - postgres
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/playoutedge
      JWT_SECRET: dev-secret
      STORAGE_MODE: LOCAL
    ports:
      - "8080:8080"
    volumes:
      - ./storage:/app/storage

volumes:
  pgdata:
```

## Trade-offs

- **Monorepo vs Multi-repo:** Monorepo для простоты шаринга кода и единого versioning
- **Exposed vs jOOQ:** Exposed для faster start; jOOQ можно добавить позже для complex queries
- **Server-side HTML vs SPA:** SSR для minimal frontend complexity
