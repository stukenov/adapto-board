# Overlay System — Design

## Context

Overlay — realtime data layer поверх видео. Архитектурные ограничения:
- SSE transport (не WebSocket) — лучше проходит через прокси
- State + domain patch — не JSON Patch RFC 6902
- Без внешних брокеров — Postgres для persistence, in-memory для pub/sub

## Goals / Non-Goals

**Goals:**
- Realtime updates (P95 ≤ 2 sec)
- Resilience при разрывах SSE
- Manual, REST pull, Webhook sources
- Widget types: text, ticker, table, KPI, QR, image

**Non-Goals:**
- Visual editor (v1)
- Complex animations
- Per-device personalization

## Decisions

### Decision 1: State + Domain Patch Model

**State:**
```json
{
  "version": 1,
  "widgets": {
    "ticker-1": {
      "type": "ticker",
      "text": "Breaking news...",
      "speed": 50,
      "position": {"x": 0, "y": 0.9, "width": 1, "height": 0.1}
    },
    "kpi-sales": {
      "type": "kpi",
      "label": "Sales Today",
      "value": "1,234",
      "position": {"x": 0.8, "y": 0.1}
    }
  }
}
```

**Patch:**
```json
{
  "version": 2,
  "upsert": [
    {"id": "kpi-sales", "value": "1,567"}
  ],
  "remove": []
}
```

**Rationale:** Domain patch по widgetId проще применить и отладить, чем JSON Patch.

### Decision 2: SSE Implementation

```
┌─────────────────────────────────────────────────────────────┐
│                          SERVER                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Manual API   │    │ REST Pull    │    │  Webhook     │  │
│  │              │    │    Job       │    │  Handler     │  │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘  │
│         │                   │                    │          │
│         └───────────────────┼────────────────────┘          │
│                             ▼                               │
│                  ┌──────────────────┐                       │
│                  │ OverlayService   │                       │
│                  │ - updateState()  │                       │
│                  │ - generatePatch()│                       │
│                  └────────┬─────────┘                       │
│                           │                                 │
│         ┌─────────────────┼─────────────────┐              │
│         ▼                 ▼                 ▼              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Postgres     │  │ In-Memory    │  │ SSE Router   │     │
│  │ (state)      │  │ Pub/Sub      │  │ (connections)│     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼ SSE
                    ┌─────────────────┐
                    │  Android TV     │
                    │  Player         │
                    └─────────────────┘
```

### Decision 3: Version Management

- `overlay_states.version` — monotonic counter
- При каждом update: version++
- Patch включает target version
- Player отслеживает последний applied version
- При mismatch: player reconnects → получает full state

### Decision 4: Reconnection Strategy

**Player:**
1. SSE disconnect detected
2. Freeze overlay (keep last state)
3. Exponential backoff reconnect (1s, 2s, 4s, 8s, max 30s)
4. On reconnect: receive full state
5. Apply new state, resume normal operation

**Server:**
- Keepalive every 15 seconds
- На reconnect: always send full state event first

### Decision 5: REST Pull Job

```kotlin
class OverlayPullJob(
    private val overlayService: OverlayService,
    private val httpClient: HttpClient
) {
    suspend fun execute(binding: OverlayBinding) {
        val response = httpClient.get(binding.sourceConfig.url) {
            // auth headers from config
        }

        val mapped = mapResponse(response, binding.sourceConfig.mapping)
        overlayService.updateState(binding.channelId, mapped)
    }
}
```

- Job scheduled по polling_interval из binding config
- Errors tracked в binding status
- Last success/error timestamps

### Decision 6: State Size Limits

- Max state size: 256KB (configurable up to 1MB)
- Validation before save
- Rejection with OVERLAY_STATE_TOO_LARGE error

## Trade-offs

- **SSE vs WebSocket:** SSE simpler, better proxy support; no bidirectional need
- **Postgres vs Redis for state:** Postgres sufficient; no additional system
- **Full state on reconnect vs delta replay:** Full state simpler; delta replay complex for minimal gain
