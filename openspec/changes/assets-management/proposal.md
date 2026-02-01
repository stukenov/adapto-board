# Assets Management — Proposal

## Why

Assets (видео и изображения) — основа контента Playout Edge. Система должна:
- Надёжно принимать upload с валидацией
- Хранить с checksums для проверки целостности
- Выдавать подписанные URL с TTL
- Работать как с локальным storage, так и с S3

## What Changes

### Upload Pipeline
- Multipart upload endpoint
- Валидация по tenant policies (codec, bitrate, resolution, size)
- Checksum SHA256 расчёт
- Статусы: UPLOADING → PROCESSING → READY/REJECTED

### Storage Abstraction
- Interface: `put`, `getSignedUrl`, `delete`
- LOCAL adapter (файловая система)
- S3 adapter (MinIO/AWS)
- Configurable через env vars

### Asset Lifecycle
- Soft delete с purge после N дней
- Asset versions (ORIGINAL/NORMALIZED) — подготовка для transcoding

### Admin API
- `POST /api/admin/assets/upload`
- `GET /api/admin/assets`
- `GET /api/admin/assets/{id}`
- `DELETE /api/admin/assets/{id}`

### Player API
- Signed URL в playlist manifest

## Capabilities

### New Capabilities
- `asset-upload`: Upload с валидацией и checksum
- `storage-abstraction`: LOCAL/S3 storage adapters
- `signed-urls`: URL с TTL для player
- `asset-validation`: Проверка по tenant policies

## Impact

- `libs/storage/` — Storage module
- `libs/domain/src/.../Asset.kt`
- `libs/persistence/src/.../AssetRepository.kt`
- `apps/server/src/.../routes/admin/AssetsRoutes.kt`
