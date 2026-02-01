# Overlay SSE Transport — Spec

## Requirements

### REQ-OVER-001: SSE Connection

#### Scenario: Connect to overlay stream

- **WHEN** player подключается к `/api/player/overlay/stream?channelId=X`
- **THEN** сервер устанавливает SSE соединение
- **AND** отправляет `event: state` с полным текущим state и version
- **AND** соединение остаётся открытым

#### Scenario: Unauthorized connection

- **WHEN** player без valid token подключается
- **THEN** сервер возвращает 401
- **AND** не устанавливает соединение

### REQ-OVER-002: State Event

#### Scenario: Initial state

- **WHEN** player подключается
- **THEN** сервер отправляет:
```
event: state
data: {"version": 1, "state": {...widgets...}}
```

#### Scenario: State on reconnect

- **WHEN** player переподключается после разрыва
- **THEN** сервер отправляет полный current state
- **AND** player заменяет локальный state

### REQ-OVER-003: Patch Event

#### Scenario: Widget update

- **WHEN** overlay state обновляется (manual/pull/webhook)
- **THEN** сервер отправляет:
```
event: patch
data: {"version": 2, "upsert": [{...widget...}], "remove": []}
```
- **AND** version = previous + 1

#### Scenario: Widget remove

- **WHEN** widget удаляется
- **THEN** patch содержит: `"remove": ["widgetId1"]`

### REQ-OVER-004: Keepalive

#### Scenario: Keepalive interval

- **WHEN** нет updates в течение N секунд
- **THEN** сервер отправляет:
```
event: keepalive
data: {"ts": 1234567890}
```

#### Scenario: Connection timeout detection

- **WHEN** player не получает событий дольше timeout
- **THEN** player переподключается

### REQ-OVER-005: Version Handling

#### Scenario: Version mismatch

- **WHEN** player получает patch с version != expected
- **THEN** player запрашивает полный state (reconnect)
- **AND** применяет новый state

#### Scenario: Sequential versions

- **WHEN** patches приходят с sequential versions
- **THEN** player применяет их по порядку

### REQ-OVER-006: Domain Patch Format

#### Scenario: Upsert operation

- **WHEN** patch содержит upsert
- **THEN** widgets с matching id обновляются
- **AND** новые widgets создаются
- **AND** операция идемпотентна

#### Scenario: Remove operation

- **WHEN** patch содержит remove
- **THEN** widgets с matching id скрываются/удаляются
- **AND** unknown widgetId игнорируются

### REQ-OVER-007: Connection Limits

#### Scenario: Max SSE connections

- **WHEN** число соединений на инстанс превышает лимит
- **THEN** новые соединения отклоняются с 503
- **AND** метрика обновляется
