# Media Validation Rules — Proposal

## Why

Asset validation критична для:
- Совместимости с Android TV устройствами
- Контроля storage и egress costs
- Предсказуемого playback quality

## What Changes

### Supported Formats (v1)

#### Video
- Container: MP4 (.mp4)
- Codec: H.264 (AVC)
- Audio: AAC
- Profile: Baseline, Main, High (до Level 4.1)

#### Image
- Formats: PNG, JPEG
- Color space: sRGB

### Validation Rules

#### Video Constraints
- Max resolution: 1920x1080 (Full HD)
- Max bitrate: 15 Mbps
- Max duration: 3600 seconds (1 hour)
- Max file size: 2 GB
- Frame rate: 24-60 fps
- Audio: stereo or mono, 44.1/48 kHz

#### Image Constraints
- Max resolution: 3840x2160 (4K)
- Max file size: 20 MB

### Tenant-Configurable Policies
Per-tenant можно ограничить:
- `max_video_resolution` (e.g., 1280x720)
- `max_video_bitrate` (e.g., 8 Mbps)
- `max_asset_size` (e.g., 500 MB)
- `allowed_codecs` (e.g., ["h264"])

### Validation Pipeline
1. Check file extension
2. Parse container metadata
3. Extract codec info (ffprobe)
4. Validate against rules
5. Calculate checksum
6. Set status: READY or REJECTED

### Rejection Reasons (human-readable)
- `UNSUPPORTED_CONTAINER`: "Only MP4 files are supported"
- `UNSUPPORTED_CODEC`: "Video codec must be H.264"
- `RESOLUTION_TOO_HIGH`: "Max resolution is 1920x1080"
- `BITRATE_TOO_HIGH`: "Max bitrate is 15 Mbps"
- `FILE_TOO_LARGE`: "Max file size is 2 GB"
- `DURATION_TOO_LONG`: "Max duration is 1 hour"
- `CORRUPT_FILE`: "File appears to be corrupted"

### UI Feedback
- Rejection reason displayed in Assets list
- "How to fix" hints
- Link to supported formats doc

### Public Documentation
- Supported media spec (for customers)
- Encoding recommendations
- Sample ffmpeg commands

## Capabilities

### New Capabilities
- `media-validation`: Asset validation pipeline
- `media-policies`: Tenant-configurable limits
- `media-rejection-reasons`: Human-readable errors

## Impact

- `libs/domain/src/.../MediaValidation.kt`
- `libs/storage/src/.../MediaValidator.kt`
- `apps/server/src/.../services/AssetValidationService.kt`
