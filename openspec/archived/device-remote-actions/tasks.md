# Device Remote Actions — Tasks

## Phase 1: Database

- [x] 1.1 Add DeviceActionType and ActionStatus enums (already existed in OpsEnums.kt)
- [x] 1.2 Create DeviceActions table (already existed in OpsTables.kt)
- [x] 1.3 Create DeviceActionEntity

## Phase 2: Repository

- [x] 2.1 Create DeviceActionRepository interface
- [x] 2.2 Create DeviceActionRepositoryImpl

## Phase 3: Service

- [x] 3.1 Create DeviceActionService

## Phase 4: Admin API

- [x] 4.1 Add POST /api/admin/devices/{id}/actions
- [x] 4.2 Add GET /api/admin/devices/{id}/actions

## Phase 5: Player API

- [x] 5.1 Add pendingActions to config response
- [x] 5.2 Add POST /api/player/actions/{id}/ack

## Phase 6: Integration

- [x] 6.1 Wire services
- [x] 6.2 Verify build
