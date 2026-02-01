# As-Run Reporting — Tasks

## Domain Layer
- [x] Create AsrunEventType enum

## Persistence Layer
- [x] Create AsrunEvents table definition
- [x] Create AsrunEventEntity
- [x] Create AsrunRepository interface
- [x] Implement AsrunRepositoryImpl

## Server Layer - Player API
- [x] Create POST /api/player/asrun batch endpoint
- [x] Validate incoming events
- [x] Store events in database

## Server Layer - Admin API
- [x] Create GET /api/admin/asrun query endpoint
- [x] Add filtering by device/channel/date
- [x] Add summary aggregation
- [x] Create GET /api/admin/asrun/export for CSV export

## Cleanup Job
- [x] Create AsrunCleanupJob
- [x] Register with JobScheduler
- [x] Implement retention policy (30 days default)

## Server Integration
- [x] Wire player asrun routes
- [x] Wire admin asrun routes

## Verification
- [x] Verify build compiles
