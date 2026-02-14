package com.playoutedge.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.llmsTxtRoutes() {
    get("/llms.txt") {
        call.respondText(LLMS_TXT, ContentType.Text.Plain)
    }

    get("/llms-full.txt") {
        call.respondText(LLMS_FULL_TXT, ContentType.Text.Plain)
    }

    get("/.well-known/ai-plugin.json") {
        call.respondText(AI_PLUGIN_JSON, ContentType.Application.Json)
    }
}

private val LLMS_TXT = """
# Playout Edge
> Multi-tenant broadcast playout management platform

## Overview
Playout Edge manages TV/digital signage channels, media assets, playback schedules,
Android-based player devices, and real-time overlay graphics. It provides a REST API,
MCP (Model Context Protocol) server, and SSR admin web interface.

## Capabilities
- Channel management (create, configure, archive)
- Asset library (upload, transcode, manage media files)
- Schedule management (draft/publish workflow with versioning and rollback)
- Device fleet management (enrollment, monitoring, remote actions)
- Overlay graphics (real-time state updates via SSE and webhooks)
- Audit logging and as-run playback reports
- Alert management (device offline, storage warnings)

## Authentication
- POST /api/auth/login with {"email", "password"} → returns JWT access token
- Include token as: Authorization: Bearer <token>
- Tokens expire in 15 minutes; use refresh token cookie for renewal

## API
- REST API: /api/admin/* (admin endpoints), /api/device/* (device endpoints)
- OpenAPI spec: /openapi/openapi.json
- Interactive docs: /docs (Redoc UI)

## MCP Server
- Endpoint: POST /api/mcp
- Transport: Streamable HTTP (JSON-RPC 2.0)
- Auth: Same Bearer JWT token as REST API
- Methods: initialize, tools/list, tools/call

## MCP Tools
- channels_list, channels_create, channels_get, channels_update, channels_delete
- assets_list, assets_get, assets_delete
- schedules_list, schedules_get, schedule_items_get, schedule_items_update, schedule_publish, schedule_rollback
- devices_list, devices_get, devices_update, devices_assign_channel, devices_actions_create
- overlay_profiles_list, overlay_profiles_create, overlay_state_get, overlay_state_update
- audit_logs, asrun_events, alerts_list

## Links
- Full documentation: /llms-full.txt
- OpenAPI specification: /openapi/openapi.json
""".trimIndent()

private val LLMS_FULL_TXT = """
# Playout Edge — Full API Documentation

## System Overview
Playout Edge is a multi-tenant broadcast playout management platform for managing
TV channels, digital signage, and media playback across Android-based player devices.

## Architecture
- Server: Kotlin/Ktor with PostgreSQL
- Multi-tenant: Each tenant has isolated data; tenant context derived from JWT claims
- Auth: Dual JWT schemes — admin tokens (15min TTL) and device tokens (1h TTL)
- Real-time: SSE for overlay state streaming, webhooks for external integrations

---

## Authentication

### Login
POST /api/auth/login
Content-Type: application/json
{"email": "admin@example.com", "password": "secret"}

Response: {"accessToken": "eyJ...", "expiresIn": 900}
Also sets refresh_token cookie (httpOnly, 7-day TTL).

### Refresh Token
POST /api/auth/refresh
(Uses refresh_token cookie automatically)

### Get Current User
GET /api/auth/me
Authorization: Bearer <token>
Response: {"id", "email", "name", "role", "tenantId"}

---

## Channels

### List Channels
GET /api/admin/channels
Authorization: Bearer <token>
Response: {"channels": [...], "total": N}

### Create Channel
POST /api/admin/channels
{"name": "Channel 1"}
Response: 201 {"id", "name", "status": "ACTIVE", "createdAt"}

### Get Channel
GET /api/admin/channels/{id}

### Update Channel
PATCH /api/admin/channels/{id}
{"name": "New Name", "status": "PAUSED"}

### Archive Channel
DELETE /api/admin/channels/{id}
Response: 204

---

## Assets

### List Assets
GET /api/admin/assets
Response: {"assets": [{id, name, type, status, mimeType, sizeBytes, durationMs, ...}], "total": N}

### Upload Asset
POST /api/admin/assets/upload
Content-Type: multipart/form-data
Form field: file (binary)
Response: 201 {id, name, type, status, storageKey, checksum, sizeBytes}

### Get Asset
GET /api/admin/assets/{id}

### Delete Asset
DELETE /api/admin/assets/{id}
Response: 204

---

## Devices

### List Devices
GET /api/admin/devices
Response: {"devices": [{id, displayName, enrollStatus, assignedChannelId, lastSeenAt, isOnline, ...}], "total", "onlineCount"}

### Get Device
GET /api/admin/devices/{id}

### Update Device
PATCH /api/admin/devices/{id}
{"displayName": "Lobby Screen"}

### Assign Channel
POST /api/admin/devices/{id}/assign-channel
{"channelId": "uuid"} (null to unassign)

### Send Action
POST /api/admin/devices/{id}/actions
{"action": "REBOOT"} (or REFRESH_PLAYLIST, SCREENSHOT, UPDATE, FACTORY_RESET)
Optional: {"params": {...}}

### Get Action History
GET /api/admin/devices/{id}/actions

---

## Schedules

Schedule versions follow a draft → publish workflow with rollback support.

### List Versions
GET /api/admin/channels/{channelId}/schedules

### Create Draft
POST /api/admin/channels/{channelId}/schedules/draft
Response: 201 {id, channelId, version, state: "DRAFT"}

### Get Version with Items
GET /api/admin/schedules/{versionId}
Response: {id, channelId, version, state, items: [{assetId, orderIndex, validFrom, validTo, daysOfWeek, timeStart, timeEnd, weight}]}

### Replace Items
PUT /api/admin/schedules/{versionId}/items
{"items": [{"assetId": "uuid", "orderIndex": 0, "weight": 1, ...}]}

### Publish
POST /api/admin/schedules/{versionId}/publish

### Rollback
POST /api/admin/channels/{channelId}/schedules/{version}/rollback

---

## Overlays

### List Profiles
GET /api/admin/overlay/profiles

### Create Profile
POST /api/admin/overlay/profiles
{"name": "News Ticker", "definition": {...}}

### Get Profile
GET /api/admin/overlay/profiles/{id}

### Get State
GET /api/admin/overlay/state/{channelId}
Response: {"channelId", "state": {...}, "version", "updatedAt"}

### Set State (Replace)
PUT /api/admin/overlay/state/{channelId}
{"state": {"widgets": {...}}}

### Patch State
PATCH /api/admin/overlay/state/{channelId}
{"upsert": [{...}], "remove": ["widgetId1"]}

---

## Audit Log
GET /api/admin/audit
Query params: entityType, entityId, actorUserId, action, from, to, limit, offset
Response: {"items": [{id, actorType, action, entityType, entityId, diff, createdAt}], "total", "limit", "offset"}

### Export Audit Log
GET /api/admin/audit/export
Query params: same as above
Response: text/csv download

## As-Run Events
GET /api/admin/asrun
Query params: deviceId, channelId, assetId, eventType, from, to, limit, offset, summary
Response: {"events": [{id, deviceId, channelId, assetId, eventType, at, details, createdAt}], "total"}

### Export As-Run
GET /api/admin/asrun/export
Query params: same as above
Response: text/csv download

## Alerts
GET /api/admin/alerts
Query params: status (ACTIVE/ACKNOWLEDGED/RESOLVED), type, limit, offset
POST /api/admin/alerts/{id}/ack — Acknowledge
POST /api/admin/alerts/{id}/resolve — Resolve

---

## Maintenance Mode
GET /api/admin/maintenance — Get maintenance status
Response: {"enabled": false, "message": null}

PUT /api/admin/maintenance — Toggle maintenance mode
{"enabled": true, "message": "Scheduled maintenance until 3pm"}

---

## Background Jobs
GET /api/admin/jobs — List registered jobs
Response: {"jobs": [{name, cronExpression, lastRun, nextRun, status}]}

GET /api/admin/jobs/{name} — Get job details

---

## Webhooks (Admin)
POST /api/admin/webhooks/{id}/regenerate — Regenerate webhook secret
GET /api/admin/webhooks/{id}/logs — Get delivery logs for a webhook

---

## Device Management (Additional)
POST /api/admin/devices/{id}/revoke — Revoke device enrollment
Response: 204

---

## Device Enrollment
1. Admin creates enrollment code: POST /api/admin/enroll-codes → {"code": "ABC123", "expiresAt": "..."}
2. Device enrolls: POST /api/device/enroll {"code": "ABC123", "deviceName": "Lobby"} → {deviceId, accessToken, refreshToken}
3. Device refreshes: POST /api/device/refresh {"refreshToken": "..."}

---

## MCP Server (Model Context Protocol)

Endpoint: POST /api/mcp
Transport: Streamable HTTP with JSON-RPC 2.0
Authentication: Bearer JWT (same as REST API)

### Initialize
{"jsonrpc": "2.0", "method": "initialize", "params": {"protocolVersion": "2025-03-26", "capabilities": {}, "clientInfo": {"name": "my-client", "version": "1.0"}}, "id": 1}

### List Tools
{"jsonrpc": "2.0", "method": "tools/list", "id": 2}

### Call Tool
{"jsonrpc": "2.0", "method": "tools/call", "params": {"name": "channels_list", "arguments": {}}, "id": 3}

### Available Tools
| Tool | Description |
|------|-------------|
| channels_list | List all channels |
| channels_create | Create a channel (name required) |
| channels_get | Get channel by ID |
| channels_update | Update channel name/status |
| channels_delete | Archive a channel |
| assets_list | List all active assets |
| assets_get | Get asset by ID |
| assets_delete | Archive an asset |
| devices_list | List devices with online status |
| devices_get | Get device details |
| devices_update | Update device display name |
| devices_assign_channel | Assign channel to device |
| devices_actions_create | Send action to device |
| schedules_list | List schedule versions for channel |
| schedules_get | Get schedule version details |
| schedule_items_get | Get items in a schedule version |
| schedule_items_update | Replace items in draft schedule |
| schedule_publish | Publish a draft schedule |
| schedule_rollback | Rollback to previous version |
| overlay_profiles_list | List overlay profiles |
| overlay_profiles_create | Create overlay profile |
| overlay_state_get | Get overlay state for channel |
| overlay_state_update | Set overlay state for channel |
| audit_logs | Query audit log |
| asrun_events | Query as-run playback events |
| alerts_list | List alerts |

---

## Data Models

### Channel
- id: UUID
- name: String
- status: ACTIVE | PAUSED | ARCHIVED
- defaultOverlayProfileId: UUID?
- createdAt: ISO 8601

### Asset
- id: UUID
- name: String (filename)
- type: VIDEO | IMAGE | AUDIO | HTML
- status: UPLOADING | READY | ARCHIVED
- mimeType: String
- sizeBytes: Long
- durationMs: Int? (for video/audio)
- width/height: Int? (for video/image)

### Device
- id: UUID
- displayName: String
- enrollStatus: ENROLLED | REJECTED
- assignedChannelId: UUID?
- lastSeenAt: ISO 8601?
- isOnline: Boolean (true if lastSeenAt < 5 min ago)
- appVersion, androidModel, androidVersion: String?

### Schedule Version
- id: UUID
- channelId: UUID
- version: Int (auto-increment per channel)
- state: DRAFT | PUBLISHED | ROLLED_BACK | SUPERSEDED
- publishedAt: ISO 8601?

### Schedule Item
- assetId: UUID
- orderIndex: Int
- validFrom/validTo: Date? (seasonal filtering)
- daysOfWeek: Int? (bitmask: 1=Mon, 2=Tue, 4=Wed, ...)
- timeStart/timeEnd: Time? (time-of-day filtering)
- weight: Int (playback weight)

---

## Error Format
All errors return: {"code": "ERROR_CODE", "message": "Description", "details": {...}, "requestId": "uuid"}
HTTP status codes: 400 (validation), 401 (auth), 403 (forbidden), 404 (not found), 409 (conflict), 500 (internal)

## Health Checks
- GET /health/live → {"status": "UP"}
- GET /health/ready → {"status": "UP/DOWN", "components": {"database": {...}, "storage": {...}}}
""".trimIndent()

private val AI_PLUGIN_JSON = """
{
  "schema_version": "v1",
  "name_for_human": "Playout Edge",
  "name_for_model": "playout_edge",
  "description_for_human": "Manage TV channels, media assets, schedules, devices, and overlay graphics for broadcast playout.",
  "description_for_model": "Multi-tenant broadcast playout management platform. Manages channels, schedules, assets, player devices, and overlay graphics. Provides REST API and MCP server for full CRUD operations. Authentication via JWT Bearer token from POST /api/auth/login.",
  "auth": {
    "type": "user_http",
    "authorization_type": "bearer"
  },
  "api": {
    "type": "openapi",
    "url": "/openapi/openapi.json"
  },
  "logo_url": "/static/logo.png",
  "contact_email": "support@adapto.kz",
  "legal_info_url": "https://tv.adapto.kz/terms"
}
""".trimIndent()
