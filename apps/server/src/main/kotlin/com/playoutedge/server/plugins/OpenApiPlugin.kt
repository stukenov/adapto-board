package com.playoutedge.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val prettyJson = Json { prettyPrint = true }

fun Application.configureOpenApi() {
    val specText = prettyJson.encodeToString(JsonObject.serializer(), Json.decodeFromString<JsonObject>(OPENAPI_SPEC_JSON))
    routing {
        get("/openapi/openapi.json") {
            call.respondText(specText, ContentType.Application.Json)
        }
    }
}

// language=JSON
private val OPENAPI_SPEC_JSON = """
{
  "openapi": "3.1.0",
  "info": {
    "title": "Playout Edge API",
    "version": "1.0.0",
    "description": "Multi-tenant broadcast playout management platform. Manages channels, schedules, assets, devices, overlays, and provides real-time monitoring via REST API and MCP (Model Context Protocol)."
  },
  "servers": [{"url": "/", "description": "Current server"}],
  "tags": [
    {"name": "Auth", "description": "User authentication"},
    {"name": "Channels", "description": "Channel management"},
    {"name": "Assets", "description": "Asset library"},
    {"name": "Schedules", "description": "Schedule versions"},
    {"name": "Devices", "description": "Device fleet"},
    {"name": "Overlays", "description": "Overlay profiles & state"},
    {"name": "Audit", "description": "Audit log"},
    {"name": "As-Run", "description": "Playback logs"},
    {"name": "Alerts", "description": "Alert management"},
    {"name": "Webhooks", "description": "Webhook endpoints"},
    {"name": "Device Auth", "description": "Device enrollment"},
    {"name": "Health", "description": "Health checks"},
    {"name": "MCP", "description": "Model Context Protocol"}
  ],
  "components": {
    "securitySchemes": {
      "bearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT",
        "description": "Admin JWT from POST /api/auth/login"
      }
    },
    "schemas": {
      "ApiError": {
        "type": "object",
        "properties": {
          "code": {"type": "string"},
          "message": {"type": "string"},
          "details": {"type": "object"},
          "requestId": {"type": "string"}
        }
      },
      "LoginRequest": {
        "type": "object",
        "required": ["email", "password"],
        "properties": {
          "email": {"type": "string"},
          "password": {"type": "string"}
        }
      },
      "LoginResponse": {
        "type": "object",
        "properties": {
          "accessToken": {"type": "string"},
          "expiresIn": {"type": "integer"}
        }
      },
      "UserInfoResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "email": {"type": "string"},
          "name": {"type": "string"},
          "role": {"type": "string", "enum": ["OWNER", "ADMIN", "OPERATOR", "VIEWER"]},
          "tenantId": {"type": "string", "format": "uuid"}
        }
      },
      "CreateChannelRequest": {
        "type": "object",
        "required": ["name"],
        "properties": {"name": {"type": "string"}}
      },
      "UpdateChannelRequest": {
        "type": "object",
        "properties": {
          "name": {"type": "string"},
          "status": {"type": "string", "enum": ["ACTIVE", "PAUSED", "ARCHIVED"]},
          "defaultOverlayProfileId": {"type": "string", "format": "uuid"}
        }
      },
      "ChannelResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "name": {"type": "string"},
          "status": {"type": "string"},
          "defaultOverlayProfileId": {"type": "string"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "ChannelsListResponse": {
        "type": "object",
        "properties": {
          "channels": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/ChannelResponse"}},
          "total": {"type": "integer"}
        }
      },
      "AssetResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "name": {"type": "string"},
          "type": {"type": "string"},
          "status": {"type": "string"},
          "mimeType": {"type": "string"},
          "sizeBytes": {"type": "integer"},
          "checksum": {"type": "string"},
          "durationMs": {"type": "integer"},
          "width": {"type": "integer"},
          "height": {"type": "integer"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "AssetsListResponse": {
        "type": "object",
        "properties": {
          "assets": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/AssetResponse"}},
          "total": {"type": "integer"}
        }
      },
      "DeviceResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "displayName": {"type": "string"},
          "enrollStatus": {"type": "string"},
          "assignedChannelId": {"type": "string"},
          "lastSeenAt": {"type": "string"},
          "appVersion": {"type": "string"},
          "androidModel": {"type": "string"},
          "androidVersion": {"type": "string"},
          "createdAt": {"type": "string", "format": "date-time"},
          "isOnline": {"type": "boolean"}
        }
      },
      "DevicesListResponse": {
        "type": "object",
        "properties": {
          "devices": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/DeviceResponse"}},
          "total": {"type": "integer"},
          "onlineCount": {"type": "integer"}
        }
      },
      "UpdateDeviceRequest": {
        "type": "object",
        "properties": {"displayName": {"type": "string"}}
      },
      "AssignChannelRequest": {
        "type": "object",
        "properties": {"channelId": {"type": "string", "format": "uuid"}}
      },
      "CreateActionRequest": {
        "type": "object",
        "required": ["action"],
        "properties": {
          "action": {"type": "string", "enum": ["REBOOT", "REFRESH_PLAYLIST", "SCREENSHOT", "UPDATE", "FACTORY_RESET"]},
          "params": {"type": "object"}
        }
      },
      "ScheduleVersionResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "channelId": {"type": "string", "format": "uuid"},
          "version": {"type": "integer"},
          "state": {"type": "string", "enum": ["DRAFT", "PUBLISHED", "SUPERSEDED"]},
          "publishedAt": {"type": "string"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "ScheduleVersionsListResponse": {
        "type": "object",
        "properties": {
          "versions": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/ScheduleVersionResponse"}},
          "total": {"type": "integer"}
        }
      },
      "ScheduleItemInput": {
        "type": "object",
        "required": ["assetId", "orderIndex"],
        "properties": {
          "assetId": {"type": "string", "format": "uuid"},
          "orderIndex": {"type": "integer"},
          "validFrom": {"type": "string", "format": "date"},
          "validTo": {"type": "string", "format": "date"},
          "daysOfWeek": {"type": "integer", "description": "Bitmask of days (1=Mon, 2=Tue, 4=Wed, ...)"},
          "timeStart": {"type": "string", "format": "time"},
          "timeEnd": {"type": "string", "format": "time"},
          "weight": {"type": "integer", "default": 1}
        }
      },
      "ReplaceItemsRequest": {
        "type": "object",
        "required": ["items"],
        "properties": {
          "items": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/ScheduleItemInput"}}
        }
      },
      "ScheduleVersionDetailResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "channelId": {"type": "string", "format": "uuid"},
          "version": {"type": "integer"},
          "state": {"type": "string"},
          "publishedAt": {"type": "string"},
          "createdAt": {"type": "string", "format": "date-time"},
          "items": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/ScheduleItemInput"}}
        }
      },
      "OverlayProfileResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "name": {"type": "string"},
          "definition": {"type": "object"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "OverlayProfilesListResponse": {
        "type": "object",
        "properties": {
          "profiles": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/OverlayProfileResponse"}},
          "total": {"type": "integer"}
        }
      },
      "CreateProfileRequest": {
        "type": "object",
        "required": ["name", "definition"],
        "properties": {
          "name": {"type": "string"},
          "definition": {"type": "object"}
        }
      },
      "OverlayStateResponse": {
        "type": "object",
        "properties": {
          "channelId": {"type": "string", "format": "uuid"},
          "state": {"type": "object"},
          "version": {"type": "integer"},
          "updatedAt": {"type": "string", "format": "date-time"}
        }
      },
      "SetStateRequest": {
        "type": "object",
        "required": ["state"],
        "properties": {"state": {"type": "object"}}
      },
      "AuditQueryResponse": {
        "type": "object",
        "properties": {
          "items": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/AuditLogResponse"}},
          "total": {"type": "integer"},
          "limit": {"type": "integer"},
          "offset": {"type": "integer"}
        }
      },
      "AuditLogResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "tenantId": {"type": "string"},
          "actorUserId": {"type": "string"},
          "actorType": {"type": "string"},
          "action": {"type": "string"},
          "entityType": {"type": "string"},
          "entityId": {"type": "string"},
          "diff": {"type": "object"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "AsrunQueryResponse": {
        "type": "object",
        "properties": {
          "events": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/AsrunEventResponse"}},
          "total": {"type": "integer"},
          "limit": {"type": "integer"},
          "offset": {"type": "integer"}
        }
      },
      "AsrunEventResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "deviceId": {"type": "string"},
          "channelId": {"type": "string"},
          "scheduleVersionId": {"type": "string"},
          "assetId": {"type": "string"},
          "eventType": {"type": "string"},
          "at": {"type": "string", "format": "date-time"},
          "details": {"type": "object"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "AlertsQueryResponse": {
        "type": "object",
        "properties": {
          "alerts": {"type": "array", "items": {"${"$"}ref": "#/components/schemas/AlertResponse"}},
          "total": {"type": "integer"},
          "limit": {"type": "integer"},
          "offset": {"type": "integer"}
        }
      },
      "AlertResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "type": {"type": "string"},
          "status": {"type": "string", "enum": ["ACTIVE", "ACKNOWLEDGED", "RESOLVED"]},
          "payload": {"type": "object"},
          "firstSeenAt": {"type": "string", "format": "date-time"},
          "lastSeenAt": {"type": "string", "format": "date-time"},
          "resolvedAt": {"type": "string"}
        }
      },
      "EnrollCodeResponse": {
        "type": "object",
        "properties": {
          "code": {"type": "string"},
          "expiresAt": {"type": "string", "format": "date-time"}
        }
      },
      "DeviceEnrollRequest": {
        "type": "object",
        "required": ["code", "deviceName"],
        "properties": {
          "code": {"type": "string"},
          "deviceName": {"type": "string"},
          "deviceModel": {"type": "string"},
          "osVersion": {"type": "string"},
          "appVersion": {"type": "string"}
        }
      },
      "DeviceEnrollResponse": {
        "type": "object",
        "properties": {
          "deviceId": {"type": "string", "format": "uuid"},
          "accessToken": {"type": "string"},
          "refreshToken": {"type": "string"},
          "expiresIn": {"type": "integer"}
        }
      },
      "HealthResponse": {
        "type": "object",
        "properties": {"status": {"type": "string"}}
      },
      "ReadinessResponse": {
        "type": "object",
        "properties": {
          "status": {"type": "string"},
          "components": {"type": "object"}
        }
      },
      "PatchStateRequest": {
        "type": "object",
        "properties": {
          "upsert": {"type": "array", "items": {"type": "object"}, "description": "Widgets to add or update"},
          "remove": {"type": "array", "items": {"type": "string"}, "description": "Widget IDs to remove"}
        }
      },
      "DeviceActionResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "action": {"type": "string"},
          "status": {"type": "string", "enum": ["PENDING", "ACKED", "FAILED", "EXPIRED"]},
          "params": {"type": "object"},
          "createdAt": {"type": "string", "format": "date-time"},
          "ackAt": {"type": "string"},
          "expiresAt": {"type": "string"}
        }
      },
      "MaintenanceRequest": {
        "type": "object",
        "required": ["enabled"],
        "properties": {
          "enabled": {"type": "boolean"},
          "reason": {"type": "string"},
          "until": {"type": "string", "format": "date-time"}
        }
      },
      "MaintenanceResponse": {
        "type": "object",
        "properties": {
          "maintenanceMode": {"type": "boolean"},
          "reason": {"type": "string"},
          "until": {"type": "string"}
        }
      },
      "JobResponse": {
        "type": "object",
        "properties": {
          "id": {"type": "string", "format": "uuid"},
          "type": {"type": "string"},
          "status": {"type": "string"},
          "attempts": {"type": "integer"},
          "maxAttempts": {"type": "integer"},
          "nextRunAt": {"type": "string", "format": "date-time"},
          "errorMessage": {"type": "string"},
          "completedAt": {"type": "string"},
          "createdAt": {"type": "string", "format": "date-time"}
        }
      },
      "DeviceRefreshRequest": {
        "type": "object",
        "required": ["refreshToken"],
        "properties": {"refreshToken": {"type": "string"}}
      },
      "DeviceRefreshResponse": {
        "type": "object",
        "properties": {
          "accessToken": {"type": "string"},
          "expiresIn": {"type": "integer"}
        }
      }
    }
  },
  "paths": {
    "/api/auth/login": {
      "post": {
        "tags": ["Auth"], "summary": "Login",
        "description": "Authenticate with email and password. Returns JWT access token and sets refresh token cookie.",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/LoginRequest"}}}},
        "responses": {
          "200": {"description": "Successful login", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/LoginResponse"}}}},
          "401": {"description": "Invalid credentials", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ApiError"}}}}
        }
      }
    },
    "/api/auth/refresh": {
      "post": {
        "tags": ["Auth"], "summary": "Refresh token",
        "description": "Refresh access token using refresh token from cookie.",
        "responses": {
          "200": {"description": "New access token", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/LoginResponse"}}}}
        }
      }
    },
    "/api/auth/logout": {
      "post": {
        "tags": ["Auth"], "summary": "Logout",
        "responses": {"204": {"description": "Logged out"}}
      }
    },
    "/api/auth/me": {
      "get": {
        "tags": ["Auth"], "summary": "Get current user",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "User info", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/UserInfoResponse"}}}}}
      }
    },
    "/api/admin/channels": {
      "get": {
        "tags": ["Channels"], "summary": "List channels",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "Channel list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ChannelsListResponse"}}}}}
      },
      "post": {
        "tags": ["Channels"], "summary": "Create channel",
        "security": [{"bearerAuth": []}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/CreateChannelRequest"}}}},
        "responses": {"201": {"description": "Created", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ChannelResponse"}}}}}
      }
    },
    "/api/admin/channels/{id}": {
      "get": {
        "tags": ["Channels"], "summary": "Get channel",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Channel", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ChannelResponse"}}}}}
      },
      "patch": {
        "tags": ["Channels"], "summary": "Update channel",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/UpdateChannelRequest"}}}},
        "responses": {"200": {"description": "Updated", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ChannelResponse"}}}}}
      },
      "delete": {
        "tags": ["Channels"], "summary": "Archive channel",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"204": {"description": "Archived"}}
      }
    },
    "/api/admin/assets": {
      "get": {
        "tags": ["Assets"], "summary": "List assets",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "Asset list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AssetsListResponse"}}}}}
      }
    },
    "/api/admin/assets/upload": {
      "post": {
        "tags": ["Assets"], "summary": "Upload asset",
        "description": "Upload media file via multipart/form-data.",
        "security": [{"bearerAuth": []}],
        "requestBody": {"required": true, "content": {"multipart/form-data": {"schema": {"type": "object", "properties": {"file": {"type": "string", "format": "binary"}}}}}},
        "responses": {"201": {"description": "Uploaded", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AssetResponse"}}}}}
      }
    },
    "/api/admin/assets/{id}": {
      "get": {
        "tags": ["Assets"], "summary": "Get asset",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Asset", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AssetResponse"}}}}}
      },
      "delete": {
        "tags": ["Assets"], "summary": "Delete asset",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"204": {"description": "Archived"}}
      }
    },
    "/api/admin/devices": {
      "get": {
        "tags": ["Devices"], "summary": "List devices",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "Device list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DevicesListResponse"}}}}}
      }
    },
    "/api/admin/devices/{id}": {
      "get": {
        "tags": ["Devices"], "summary": "Get device",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Device", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceResponse"}}}}}
      },
      "patch": {
        "tags": ["Devices"], "summary": "Update device",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/UpdateDeviceRequest"}}}},
        "responses": {"200": {"description": "Updated", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceResponse"}}}}}
      }
    },
    "/api/admin/devices/{id}/assign-channel": {
      "post": {
        "tags": ["Devices"], "summary": "Assign channel to device",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AssignChannelRequest"}}}},
        "responses": {"200": {"description": "Assigned", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceResponse"}}}}}
      }
    },
    "/api/admin/devices/{id}/actions": {
      "post": {
        "tags": ["Devices"], "summary": "Create device action",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/CreateActionRequest"}}}},
        "responses": {"201": {"description": "Action created"}}
      },
      "get": {
        "tags": ["Devices"], "summary": "Get device actions",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Action history"}}
      }
    },
    "/api/admin/channels/{channelId}/schedules": {
      "get": {
        "tags": ["Schedules"], "summary": "List schedule versions",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Version list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ScheduleVersionsListResponse"}}}}}
      }
    },
    "/api/admin/channels/{channelId}/schedules/draft": {
      "post": {
        "tags": ["Schedules"], "summary": "Create draft schedule",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"201": {"description": "Draft created", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ScheduleVersionResponse"}}}}}
      }
    },
    "/api/admin/channels/{channelId}/schedules/{version}/rollback": {
      "post": {
        "tags": ["Schedules"], "summary": "Rollback schedule",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}},
          {"name": "version", "in": "path", "required": true, "schema": {"type": "integer"}}
        ],
        "responses": {"200": {"description": "Rolled back", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ScheduleVersionResponse"}}}}}
      }
    },
    "/api/admin/schedules/{versionId}": {
      "get": {
        "tags": ["Schedules"], "summary": "Get schedule version with items",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "versionId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Version detail", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ScheduleVersionDetailResponse"}}}}}
      }
    },
    "/api/admin/schedules/{versionId}/items": {
      "put": {
        "tags": ["Schedules"], "summary": "Replace schedule items",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "versionId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ReplaceItemsRequest"}}}},
        "responses": {"200": {"description": "Items replaced"}}
      }
    },
    "/api/admin/schedules/{versionId}/publish": {
      "post": {
        "tags": ["Schedules"], "summary": "Publish schedule",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "versionId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Published"}}
      }
    },
    "/api/admin/overlay/profiles": {
      "get": {
        "tags": ["Overlays"], "summary": "List overlay profiles",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "Profile list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayProfilesListResponse"}}}}}
      },
      "post": {
        "tags": ["Overlays"], "summary": "Create overlay profile",
        "security": [{"bearerAuth": []}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/CreateProfileRequest"}}}},
        "responses": {"201": {"description": "Created", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayProfileResponse"}}}}}
      }
    },
    "/api/admin/overlay/profiles/{id}": {
      "get": {
        "tags": ["Overlays"], "summary": "Get overlay profile",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Profile", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayProfileResponse"}}}}}
      }
    },
    "/api/admin/overlay/state/{channelId}": {
      "get": {
        "tags": ["Overlays"], "summary": "Get overlay state",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "State", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayStateResponse"}}}}}
      },
      "put": {
        "tags": ["Overlays"], "summary": "Replace overlay state",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/SetStateRequest"}}}},
        "responses": {"200": {"description": "Updated", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayStateResponse"}}}}}
      },
      "patch": {
        "tags": ["Overlays"], "summary": "Patch overlay state",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "channelId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/PatchStateRequest"}}}},
        "responses": {"200": {"description": "Patched", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/OverlayStateResponse"}}}}}
      }
    },
    "/api/admin/audit": {
      "get": {
        "tags": ["Audit"], "summary": "Query audit log",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "entityType", "in": "query", "schema": {"type": "string"}},
          {"name": "entityId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "actorUserId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "action", "in": "query", "schema": {"type": "string"}},
          {"name": "from", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "to", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 100}},
          {"name": "offset", "in": "query", "schema": {"type": "integer", "default": 0}}
        ],
        "responses": {"200": {"description": "Audit entries", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AuditQueryResponse"}}}}}
      }
    },
    "/api/admin/asrun": {
      "get": {
        "tags": ["As-Run"], "summary": "Query as-run events",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "deviceId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "channelId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "assetId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "eventType", "in": "query", "schema": {"type": "string", "enum": ["PLAY_START", "PLAY_END", "ERROR"]}},
          {"name": "from", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "to", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 100}},
          {"name": "offset", "in": "query", "schema": {"type": "integer", "default": 0}},
          {"name": "summary", "in": "query", "schema": {"type": "boolean", "default": false}}
        ],
        "responses": {"200": {"description": "As-run events", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AsrunQueryResponse"}}}}}
      }
    },
    "/api/admin/alerts": {
      "get": {
        "tags": ["Alerts"], "summary": "List alerts",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "status", "in": "query", "schema": {"type": "string", "enum": ["ACTIVE", "ACKNOWLEDGED", "RESOLVED"]}},
          {"name": "type", "in": "query", "schema": {"type": "string"}},
          {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 50}},
          {"name": "offset", "in": "query", "schema": {"type": "integer", "default": 0}}
        ],
        "responses": {"200": {"description": "Alert list", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AlertsQueryResponse"}}}}}
      }
    },
    "/api/admin/alerts/{id}/ack": {
      "post": {
        "tags": ["Alerts"], "summary": "Acknowledge alert",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Acknowledged", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AlertResponse"}}}}}
      }
    },
    "/api/admin/alerts/{id}/resolve": {
      "post": {
        "tags": ["Alerts"], "summary": "Resolve alert",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Resolved", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/AlertResponse"}}}}}
      }
    },
    "/api/admin/enroll-codes": {
      "post": {
        "tags": ["Device Auth"], "summary": "Create enrollment code",
        "security": [{"bearerAuth": []}],
        "responses": {"201": {"description": "Code generated", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/EnrollCodeResponse"}}}}}
      }
    },
    "/api/device/enroll": {
      "post": {
        "tags": ["Device Auth"], "summary": "Enroll device",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceEnrollRequest"}}}},
        "responses": {"201": {"description": "Enrolled", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceEnrollResponse"}}}}}
      }
    },
    "/api/webhook/{bindingId}": {
      "post": {
        "tags": ["Webhooks"], "summary": "Receive webhook",
        "description": "Public endpoint for external systems to push overlay data via webhook.",
        "parameters": [{"name": "bindingId", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Accepted"}}
      }
    },
    "/health/live": {
      "get": {
        "tags": ["Health"], "summary": "Liveness probe",
        "responses": {"200": {"description": "Alive", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/HealthResponse"}}}}}
      }
    },
    "/health/ready": {
      "get": {
        "tags": ["Health"], "summary": "Readiness probe",
        "responses": {
          "200": {"description": "Ready", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/ReadinessResponse"}}}},
          "503": {"description": "Not ready"}
        }
      }
    },
    "/api/admin/audit/export": {
      "get": {
        "tags": ["Audit"], "summary": "Export audit log as CSV",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "entityType", "in": "query", "schema": {"type": "string"}},
          {"name": "from", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "to", "in": "query", "schema": {"type": "string", "format": "date-time"}}
        ],
        "responses": {"200": {"description": "CSV file", "content": {"text/csv": {"schema": {"type": "string"}}}}}
      }
    },
    "/api/admin/asrun/export": {
      "get": {
        "tags": ["As-Run"], "summary": "Export as-run events as CSV",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "deviceId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "channelId", "in": "query", "schema": {"type": "string", "format": "uuid"}},
          {"name": "from", "in": "query", "schema": {"type": "string", "format": "date-time"}},
          {"name": "to", "in": "query", "schema": {"type": "string", "format": "date-time"}}
        ],
        "responses": {"200": {"description": "CSV file", "content": {"text/csv": {"schema": {"type": "string"}}}}}
      }
    },
    "/api/admin/devices/{id}/revoke": {
      "post": {
        "tags": ["Device Auth"], "summary": "Revoke device",
        "description": "Revoke device enrollment. Requires DEVICES_REVOKE permission.",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"204": {"description": "Device revoked"}}
      }
    },
    "/api/device/refresh": {
      "post": {
        "tags": ["Device Auth"], "summary": "Refresh device token",
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceRefreshRequest"}}}},
        "responses": {"200": {"description": "New access token", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/DeviceRefreshResponse"}}}}}
      }
    },
    "/api/admin/tenant/maintenance": {
      "get": {
        "tags": ["Admin"], "summary": "Get maintenance status",
        "security": [{"bearerAuth": []}],
        "responses": {"200": {"description": "Maintenance status", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/MaintenanceResponse"}}}}}
      },
      "put": {
        "tags": ["Admin"], "summary": "Toggle maintenance mode",
        "security": [{"bearerAuth": []}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/MaintenanceRequest"}}}},
        "responses": {"200": {"description": "Updated", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/MaintenanceResponse"}}}}}
      }
    },
    "/api/admin/jobs": {
      "get": {
        "tags": ["Admin"], "summary": "List background jobs",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "limit", "in": "query", "schema": {"type": "integer", "default": 50}}],
        "responses": {"200": {"description": "Job list"}}
      }
    },
    "/api/admin/jobs/{id}": {
      "get": {
        "tags": ["Admin"], "summary": "Get job details",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "Job details", "content": {"application/json": {"schema": {"${"$"}ref": "#/components/schemas/JobResponse"}}}}}
      }
    },
    "/api/admin/overlays/bindings/{id}/webhook/regenerate": {
      "post": {
        "tags": ["Webhooks"], "summary": "Regenerate webhook secret",
        "security": [{"bearerAuth": []}],
        "parameters": [{"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}}],
        "responses": {"200": {"description": "New secret and URL"}}
      }
    },
    "/api/admin/overlays/bindings/{id}/webhook/logs": {
      "get": {
        "tags": ["Webhooks"], "summary": "Get webhook delivery logs",
        "security": [{"bearerAuth": []}],
        "parameters": [
          {"name": "id", "in": "path", "required": true, "schema": {"type": "string", "format": "uuid"}},
          {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 100}}
        ],
        "responses": {"200": {"description": "Webhook logs"}}
      }
    },
    "/api/mcp": {
      "post": {
        "tags": ["MCP"], "summary": "MCP JSON-RPC endpoint",
        "description": "Model Context Protocol (MCP) server endpoint. Supports JSON-RPC 2.0 methods: initialize, tools/list, tools/call. Use the same Bearer JWT token as REST API.",
        "security": [{"bearerAuth": []}],
        "requestBody": {"required": true, "content": {"application/json": {"schema": {"type": "object", "properties": {"jsonrpc": {"type": "string", "const": "2.0"}, "method": {"type": "string"}, "params": {"type": "object"}, "id": {"type": "integer"}}}}}},
        "responses": {"200": {"description": "JSON-RPC response"}}
      }
    }
  }
}
""".trimIndent()
