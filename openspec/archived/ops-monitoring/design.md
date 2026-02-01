# Ops & Monitoring — Design

## Overview

Operational monitoring capabilities for support-grade UX, enabling fast diagnosis of issues without lengthy back-and-forth.

## Health Endpoints

### Liveness Check
```
GET /health/live
Response: { "status": "UP" }
```
Simple check that the application is running.

### Readiness Check
```
GET /health/ready
Response: {
  "status": "UP",
  "components": {
    "database": { "status": "UP", "latencyMs": 5 },
    "storage": { "status": "UP" }
  }
}
```
Checks database connection and storage availability.

## Alerts System

### Data Model
```sql
-- Already exists in OpsTables.Alerts
-- tenantId, type, status, payload, createdAt, acknowledgedAt, resolvedAt
```

### Alert Types
- `ONLINE_RATE_LOW` - Too many devices offline
- `PUBLISH_FAILURE_SPIKE` - High rate of schedule publish failures
- `CONNECTOR_FAILURE` - External connector failed
- `DB_DOWN` - Database connection issues
- `APP_DOWN` - Application health check failed

### Alert Status Flow
OPEN -> ACKED -> RESOLVED

### Admin API
```
GET /api/admin/alerts?status=OPEN&limit=50
POST /api/admin/alerts/{id}/ack
POST /api/admin/alerts/{id}/resolve
```

## Request Correlation

Already implemented via RequestIdPlugin:
- Generates UUID for each request
- Available via X-Request-Id header
- Logged in all request/response logs

## Metrics Preparation

For future Prometheus integration:
- `observability` library already exists
- Can add Micrometer integration later
- Current focus: Health endpoints + Alerts API

## Components

### Server Layer
- `HealthRoutes.kt` - Health check endpoints
- `AlertsRoutes.kt` - Alerts admin API
- `AlertService.kt` - Alert business logic

### Persistence Layer
- Use existing `Alerts` table in OpsTables
- Create `AlertEntity` in OpsEntities
- Create `AlertRepository` interface and implementation

## Security

- Health endpoints are public (for load balancer probes)
- Alerts endpoints require admin authentication
- Alerts are tenant-scoped
