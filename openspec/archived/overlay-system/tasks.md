# Overlay System — Tasks

## Phase 1: Repository Layer

- [x] 1.1 Create OverlayRepository interface
- [x] 1.2 Create OverlayRepositoryImpl with CRUD for profiles
- [x] 1.3 Add state get/set/update methods

## Phase 2: Pub/Sub Infrastructure

- [x] 2.1 Create OverlaySubscribers in-memory pub/sub
- [x] 2.2 Create OverlayEvent sealed class (State, Patch, Keepalive)
- [x] 2.3 Implement broadcast to channel subscribers

## Phase 3: Service Layer

- [x] 3.1 Create OverlayService with profile management
- [x] 3.2 Add setState and patchState methods
- [x] 3.3 Integrate with OverlaySubscribers for broadcast

## Phase 4: Admin API

- [x] 4.1 Create OverlayRoutes with GET/POST /api/admin/overlay/profiles
- [x] 4.2 Add GET /api/admin/overlay/profiles/{id}
- [x] 4.3 Add GET /api/admin/overlay/state/{channelId}
- [x] 4.4 Add PUT /api/admin/overlay/state/{channelId}
- [x] 4.5 Add PATCH /api/admin/overlay/state/{channelId}

## Phase 5: Player SSE Stream

- [x] 5.1 Create OverlayStreamRoutes with SSE endpoint
- [x] 5.2 Implement state event on connect
- [x] 5.3 Implement patch event forwarding
- [x] 5.4 Implement keepalive every 15 seconds

## Phase 6: Integration

- [x] 6.1 Wire services into Application.module()
- [x] 6.2 Register routes in server routing
- [x] 6.3 Verify build compiles
