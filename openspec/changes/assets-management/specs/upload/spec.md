# Asset Upload — Spec

## Requirements

### REQ-ASSET-001: Upload Endpoint

#### Scenario: Successful upload

- **WHEN** operator загружает valid файл (MP4 H.264/AAC или PNG/JPEG)
- **THEN** система сохраняет файл в storage
- **AND** вычисляет checksum SHA256
- **AND** создаёт asset record со статусом PROCESSING
- **AND** валидирует по tenant policies
- **AND** устанавливает статус READY или REJECTED

#### Scenario: Rejected by codec

- **WHEN** operator загружает файл с неподдерживаемым кодеком
- **THEN** система устанавливает статус REJECTED
- **AND** сохраняет причину "Codec not supported: {codec}"

#### Scenario: Rejected by size

- **WHEN** operator загружает файл больше max_asset_size
- **THEN** система отклоняет upload с ошибкой ASSET_TOO_LARGE

#### Scenario: Quota exceeded

- **WHEN** operator загружает файл
- **AND** storage quota превышена
- **THEN** система отклоняет с ошибкой TENANT_QUOTA_EXCEEDED

### REQ-ASSET-002: Validation Rules

#### Scenario: Video validation

- **WHEN** загружается видео
- **THEN** система проверяет: codec (H.264), container (MP4), bitrate ≤ max, resolution ≤ max
- **AND** извлекает duration_ms, width, height

#### Scenario: Image validation

- **WHEN** загружается изображение
- **THEN** система проверяет: format (PNG/JPEG), resolution ≤ max
- **AND** извлекает width, height

### REQ-ASSET-003: Storage

#### Scenario: LOCAL storage

- **WHEN** STORAGE_MODE=LOCAL
- **THEN** файлы сохраняются в filesystem
- **AND** storage_key = relative path

#### Scenario: S3 storage

- **WHEN** STORAGE_MODE=S3
- **THEN** файлы сохраняются в S3 bucket
- **AND** storage_key = S3 key

### REQ-ASSET-004: Signed URLs

#### Scenario: Get signed URL

- **WHEN** player запрашивает asset URL
- **THEN** система возвращает signed URL
- **AND** URL содержит expiry timestamp
- **AND** URL привязан к tenant_id и asset_id

#### Scenario: Expired URL

- **WHEN** player использует expired URL
- **THEN** storage возвращает 403
- **AND** player должен запросить новый manifest

### REQ-ASSET-005: Lifecycle

#### Scenario: Soft delete

- **WHEN** operator удаляет asset
- **THEN** статус меняется на DELETED
- **AND** asset не появляется в списках
- **AND** нельзя добавить в новые schedules

#### Scenario: Purge after retention

- **WHEN** asset в статусе DELETED дольше retention period
- **THEN** cleanup job удаляет файл из storage
- **AND** удаляет record из DB
