# As-Run Reporting — Tasks

## Database

- [x] AsrunEvents table already exists in AuditTables.kt
- [x] AsrunEventEntity already exists in AuditEntities.kt
- [x] Add HEARTBEAT to AsrunEventType enum
- [x] Create AsrunRepository interface
- [x] Implement AsrunRepositoryImpl with tenant-scoped queries

## Server

- [x] Create AsrunService for business logic
- [x] Create player asrun routes (POST /api/player/asrun)
- [x] Create admin asrun routes (GET /api/admin/asrun)
- [x] Create admin export endpoint (GET /api/admin/asrun/export)
- [x] Create AsrunCleanupJobHandler
- [x] Register cleanup handler in Application.kt
- [x] Wire routes in Application.kt

## Verification

- [x] Verify build compiles
