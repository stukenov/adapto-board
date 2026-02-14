# Playout Edge

TV broadcast playout automation platform — schedule, overlay, and device management for broadcast channels.

## Architecture

- **Runtime**: Kotlin/JVM with Ktor (Netty)
- **SSR**: kotlinx.html (streaming mode)
- **Database**: PostgreSQL via Exposed DAO (lazy entity references)
- **Auth**: JWT (HMAC256) — admin + device tokens
- **Deploy**: Docker containers on `tv.adapto.kz` via Ansible

## Project Structure

```
apps/server/          — Ktor application (routes, services, MCP server)
libs/persistence/     — Exposed DAO entities, tables, migrations
libs/auth/            — JWT auth, password hashing, session management
deploy/               — Ansible playbook, Docker Compose, nginx config
scripts/              — Test & utility scripts
```

## Build & Deploy

```bash
# Build (skip tests)
./gradlew :apps:server:build -x test

# Deploy to production
cd deploy && ansible-playbook -i inventory.ini playbook.yml
```

## REST API

All admin endpoints require `Authorization: Bearer <token>` from `POST /api/auth/login`.

| Category   | Endpoints                                                              |
|------------|------------------------------------------------------------------------|
| Auth       | `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/refresh`  |
| Channels   | `GET/POST /api/admin/channels`, `GET/PATCH/DELETE .../channels/{id}`   |
| Assets     | `GET /api/admin/assets`, `GET/DELETE .../assets/{id}`, `POST .../upload` |
| Devices    | `GET /api/admin/devices`, `GET/PATCH .../devices/{id}`, assign/actions |
| Schedules  | `POST .../schedules/draft`, `GET/PUT .../items`, `POST .../publish`    |
| Overlays   | `GET/POST .../overlay/profiles`, `GET/PUT/PATCH .../overlay/state/{channelId}` |
| Audit      | `GET /api/admin/audit`, `GET .../audit/export`                        |
| As-Run     | `GET /api/admin/asrun`, `GET .../asrun/export`                        |
| Alerts     | `GET /api/admin/alerts`, `POST .../alerts/{id}/ack`, `.../resolve`    |
| Health     | `GET /health/live`, `GET /health/ready`                                |

## MCP Server

Endpoint: `POST /api/mcp` (JSON-RPC 2.0, requires admin JWT).

**Tools** (24+): `channels_list`, `channels_create`, `channels_get`, `channels_update`, `channels_delete`, `assets_list`, `assets_get`, `assets_delete`, `devices_list`, `devices_get`, `devices_update`, `devices_assign_channel`, `devices_actions_create`, `schedules_list`, `schedules_get`, `schedule_items_get`, `schedule_items_update`, `schedule_publish`, `schedule_rollback`, `overlay_profiles_list`, `overlay_profiles_create`, `overlay_state_get`, `overlay_state_update`, `audit_logs`, `asrun_events`, `alerts_list`

Use via Claude Code: MCP proxy configured in `.mcp.json`.

## Key Conventions

1. **Exposed DAO lazy refs**: Always map entities to DTOs INSIDE `newSuspendedTransaction {}` blocks
2. **kotlinx.html streaming**: Attributes (`id =`, `classes`) BEFORE content (`+text`, child tags)
3. **Tenant isolation**: All queries scoped by `tenantId` from JWT claims
