# As-Run Reporting — Design

## Overview

As-run reporting tracks what content was actually displayed on devices. This is essential for compliance, billing verification, and troubleshooting playback issues.

## Data Model

### AsrunEvents Table

```sql
CREATE TABLE asrun_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    device_id UUID NOT NULL REFERENCES devices(id),
    channel_id UUID REFERENCES channels(id),
    schedule_id UUID REFERENCES schedules(id),
    asset_id UUID REFERENCES assets(id),
    event_type VARCHAR(20) NOT NULL, -- START, END, HEARTBEAT, ERROR
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    duration_ms INTEGER,
    details JSONB, -- error info, metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_asrun_events_tenant_device ON asrun_events(tenant_id, device_id, started_at);
CREATE INDEX idx_asrun_events_tenant_channel ON asrun_events(tenant_id, channel_id, started_at);
CREATE INDEX idx_asrun_events_created ON asrun_events(created_at);
```

### Event Types

- `START` - Asset playback started
- `END` - Asset playback completed normally
- `HEARTBEAT` - Periodic snapshot during long playback
- `ERROR` - Playback interrupted due to error

## API Design

### Player Endpoint (Batch Upload)

```
POST /api/player/asrun
Authorization: Bearer <device-token>
Content-Type: application/json

{
  "events": [
    {
      "channelId": "uuid",
      "scheduleId": "uuid",
      "assetId": "uuid",
      "eventType": "START",
      "startedAt": "2024-01-15T10:00:00Z",
      "endedAt": null,
      "durationMs": null,
      "details": {}
    }
  ]
}

Response: { "accepted": 5 }
```

### Admin Query Endpoint

```
GET /api/admin/asrun?deviceId=&channelId=&from=&to=&limit=100
Authorization: Bearer <admin-token>

Response: {
  "events": [...],
  "summary": {
    "totalEvents": 150,
    "totalDurationMs": 3600000,
    "byAsset": {...}
  }
}
```

### Admin Export

```
GET /api/admin/asrun/export?format=csv&deviceId=&from=&to=
Authorization: Bearer <admin-token>
Accept: text/csv

Response: CSV file download
```

## Components

### Domain Layer
- `AsrunEventType` enum in domain/enums
- Event processing logic

### Persistence Layer
- `AsrunEvents` table definition
- `AsrunEventEntity` exposed entity
- `AsrunRepository` interface and implementation

### Server Layer
- `AsrunService` for business logic
- Player routes for batch upload
- Admin routes for querying and export
- Cleanup job handler for retention

## Retention & Cleanup

- Default retention: 30 days
- Configurable per-tenant via env or settings
- Cleanup job runs daily, deletes events older than retention period
- Registers with existing JobScheduler as ASRUN_CLEANUP type

## Security

- Player endpoint requires device JWT token
- Admin endpoints require admin JWT with tenant context
- Events are tenant-scoped, no cross-tenant access
