# Admin Web Assets — Proposal

## Why

Assets (видео/изображения) — библиотека контента. Operator должен легко загружать, находить и управлять assets.

## What Changes

### Assets List
- Grid/list view toggle
- Thumbnails для изображений
- Filters: type (VIDEO/IMAGE), status, search
- Pagination
- Sort by date/name/size

### Upload Flow
- Drag & drop zone
- Multi-file upload
- Progress per file
- Status pipeline: Uploading → Validating → Ready/Rejected
- Rejected reason с "как исправить"

### Asset Detail
- Preview (video player / image)
- Metadata (name, size, duration, resolution, codec)
- Status и validation info
- Edit name
- Delete (soft delete with confirmation)
- Usage: "Used in schedules: ..."

### Quota Management
- Storage usage indicator
- Quota exceeded warning
- CTA: "Delete assets" / "Request upgrade"

### Validation Feedback
- Human-readable reject reasons
- Policy info (allowed codecs, max bitrate, etc.)
- "How to fix" hints

### Empty State
- "No assets yet"
- "Upload your first video or image"
- Sample pack suggestion

## Capabilities

### New Capabilities
- `admin-assets-list`: Список assets с фильтрами
- `admin-asset-upload`: Upload с progress и validation
- `admin-asset-detail`: Детальная страница asset
- `admin-asset-quota`: Отображение квоты

## Impact

- `apps/server/src/.../routes/admin/AssetRoutes.kt`
- `apps/server/src/.../views/assets/*.kt`
