# Admin Web Overlay — Proposal

## Why

Overlay management UI для настройки data layer. Без визуального редактора (v1), но с полным функционалом: profiles, bindings, manual editor, connectors.

## What Changes

### Overlay Profiles
- List: name, widget types, usage count
- Create profile form:
  - Template selection (Ticker, KPI tiles, Queue table, QR card)
  - Position, sizes, basic styles
  - JSON preview
- Edit profile
- Delete (with usage warning)

### Overlay Bindings
- List per channel (Channel → Overlay tab)
- Create binding:
  - Choose profile
  - Choose source type: Manual / REST Pull / Webhook
  - Source config
- Edit binding
- Enable/disable

### Manual Editor
- Form per widget type
- Field editors (text, number, list)
- Preview (text representation)
- "Send" button
- Applied tracker: X/Y devices, latency

### REST Pull Connector
- URL input
- Auth config (none, bearer, basic, custom header)
- Polling interval
- Mapping preset (queue/kpi/ticker) + field mapping
- Test fetch button:
  - Raw response
  - Mapped preview
  - Validation
- Status: last success, last error, next poll

### Webhook Config
- Endpoint URL (auto-generated)
- Signing secret (show/copy)
- Example payload
- Logs: last 20 calls (status, latency, errors)

### Overlay Health
- Connector status cards
- Last success/error timestamps
- Error details
- Test button

### Size Limits
- State size indicator
- Warning when approaching limit
- Validation on save

## Capabilities

### New Capabilities
- `admin-overlay-profiles`: CRUD overlay profiles
- `admin-overlay-bindings`: Channel-profile-source связь
- `admin-overlay-manual-editor`: Ручное редактирование данных
- `admin-overlay-rest-connector`: REST pull настройка
- `admin-overlay-webhook-config`: Webhook endpoint настройка
- `admin-overlay-health`: Статус коннекторов

## Impact

- `apps/server/src/.../routes/admin/OverlayRoutes.kt`
- `apps/server/src/.../views/overlay/*.kt`
