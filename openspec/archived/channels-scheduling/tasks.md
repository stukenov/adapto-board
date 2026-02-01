# Channels & Scheduling — Tasks

## Phase 1: Domain Services

- [x] 1.1 Create ChannelService with CRUD operations
- [x] 1.2 Create ScheduleService with draft/publish workflow
- [x] 1.3 Create PlaylistService with manifest generation
- [x] 1.4 Add PublishResult and RollbackResult sealed classes

## Phase 2: Repository Extensions

- [x] 2.1 Add setActiveVersion method to ScheduleRepository
- [x] 2.2 Add replaceItems method to ScheduleRepository (atomic replace)
- [x] 2.3 Add findPublishedVersion method for rollback lookup
- [x] 2.4 Update ScheduleRepositoryImpl with new methods

## Phase 3: Admin API - Channels

- [x] 3.1 Create ChannelsRoutes with POST /api/admin/channels
- [x] 3.2 Add GET /api/admin/channels (list)
- [x] 3.3 Add GET /api/admin/channels/{id}
- [x] 3.4 Add PATCH /api/admin/channels/{id}
- [x] 3.5 Add DELETE /api/admin/channels/{id} (archive)

## Phase 4: Admin API - Schedules

- [x] 4.1 Create SchedulesRoutes with POST /channels/{id}/schedules/draft
- [x] 4.2 Add GET /channels/{id}/schedules (list versions)
- [x] 4.3 Add GET /schedules/{versionId}
- [x] 4.4 Add PUT /schedules/{versionId}/items
- [x] 4.5 Add POST /schedules/{versionId}/publish
- [x] 4.6 Add POST /channels/{id}/schedules/{version}/rollback

## Phase 5: Player API

- [x] 5.1 Create PlaylistRoutes with GET /api/player/playlist
- [x] 5.2 Wire manifest generation with signed URLs

## Phase 6: Integration

- [x] 6.1 Wire services into Application.module()
- [x] 6.2 Register routes in server routing
- [x] 6.3 Verify build compiles
