# Device Remote Actions — Proposal

## Why

Remote actions позволяют support быстро решать проблемы без физического доступа к устройству:
- Принудительное обновление config
- Принудительное обновление playlist
- Ротация токена при security triage

## What Changes

### Actions Table
`device_actions`:
- id (uuid)
- tenant_id
- device_id
- action (enum)
- params_json (optional)
- status (PENDING, ACKED, FAILED, EXPIRED)
- created_by (user_id)
- created_at
- ack_at (nullable)
- expires_at

### Action Types (v1)
- `FORCE_CONFIG_REFRESH` — немедленный poll config
- `FORCE_PLAYLIST_REFRESH` — немедленный fetch playlist
- `ROTATE_DEVICE_TOKEN` — инвалидирует текущий token

### Action Types (R1)
- `REBOOT` — перезагрузка устройства (если MDM позволяет)
- `CLEAR_CACHE` — очистка media cache
- `COLLECT_LOGS` — сбор логов для support

### Admin API
- `POST /api/admin/devices/{id}/actions` — создать action
  - Требует SupportAdmin роль
  - Confirmation modal + reason
- `GET /api/admin/devices/{id}/actions` — history

### Player Flow
1. Player делает heartbeat/config poll
2. В response включены pending actions
3. Player выполняет action
4. Player отправляет ack: `POST /api/player/actions/{id}/ack`

### Player API
- Actions в response `/api/player/config`:
  ```json
  {
    ...,
    "pendingActions": [
      {"id": "...", "action": "FORCE_PLAYLIST_REFRESH"}
    ]
  }
  ```
- `POST /api/player/actions/{id}/ack` — подтверждение

### Action Lifecycle
- Created → PENDING
- Player acks → ACKED
- Timeout (e.g., 1 hour) → EXPIRED
- Player reports failure → FAILED

### Audit
- All actions logged to audit
- Include reason and initiator

## Capabilities

### New Capabilities
- `device-remote-actions`: Удалённые команды устройствам
- `device-action-ack`: Player acknowledgment
- `device-action-history`: История действий

## Impact

- `libs/persistence/src/.../DeviceAction.kt`
- `apps/server/src/.../routes/admin/DeviceActionsRoutes.kt`
- `apps/server/src/.../routes/player/ActionsRoutes.kt`
- `apps/player-androidtv/src/.../actions/ActionHandler.kt`
