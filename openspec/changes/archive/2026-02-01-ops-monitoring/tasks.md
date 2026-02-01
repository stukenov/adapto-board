# Ops & Monitoring — Tasks

## Health Endpoints
- [x] Create HealthRoutes with /health/live
- [x] Add /health/ready with DB and storage checks
- [x] Return JSON status with component details

## Alerts System
- [x] Create AlertEntity in persistence
- [x] Create AlertRepository interface and implementation
- [x] Create AlertService for business logic
- [x] Create AlertsRoutes for admin API

## Admin API
- [x] GET /api/admin/alerts endpoint
- [x] POST /api/admin/alerts/{id}/ack endpoint
- [x] POST /api/admin/alerts/{id}/resolve endpoint

## Server Integration
- [x] Wire health routes in Application.kt
- [x] Wire alerts routes in Application.kt

## Verification
- [x] Verify build compiles
