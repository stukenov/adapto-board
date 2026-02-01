# Device Management — Tasks

## Phase 1: Service Layer

- [x] 1.1 Create DeviceService with fleet management methods
- [x] 1.2 Add online status calculation logic
- [x] 1.3 Add channel assignment method

## Phase 2: Repository Extensions

- [x] 2.1 Add updateHeartbeat method to DeviceRepository
- [x] 2.2 Add findOnline method (devices seen within threshold)
- [x] 2.3 Update DeviceRepositoryImpl with new methods

## Phase 3: Admin API

- [x] 3.1 Create DevicesRoutes with GET /api/admin/devices
- [x] 3.2 Add GET /api/admin/devices/{id}
- [x] 3.3 Add PATCH /api/admin/devices/{id}
- [x] 3.4 Add POST /api/admin/devices/{id}/assign-channel

## Phase 4: Player API

- [x] 4.1 Create DevicePlayerRoutes with GET /api/player/config
- [x] 4.2 Add POST /api/player/heartbeat

## Phase 5: Integration

- [x] 5.1 Wire DeviceService into Application.module()
- [x] 5.2 Register routes in server routing
- [x] 5.3 Verify build compiles
