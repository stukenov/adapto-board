# Player Cache Management — Proposal

## Why

Cache — критичный компонент для offline playback. Player должен работать даже при отсутствии сети, используя закэшированный контент.

## What Changes

### Media Cache
- ExoPlayer/Media3 disk cache
- Configurable quota (4-16 GB)
- Cache directory management

### Download Manager
- Background downloads
- Priority queue (current version first)
- Pause/resume support
- Progress tracking

### Cache Policy
- LRU eviction
- "Pin" current active version
- Pre-download next version (hint from server)
- Cleanup old versions

### Checksum Validation
- SHA256 validation after download
- Reject corrupted files
- Re-download on failure

### Quota Management
- Track used space
- Enforce hard limit
- Warn when approaching limit
- Smart eviction

### Asset Availability
- Track which assets are cached
- Report cache status
- Handle missing assets (download or skip)

### Offline Mode
- Detect offline state
- Switch to cache-only playback
- Resume downloads when online

### Cache Warming
- Download all assets for current playlist
- Background prefetch
- Network-aware (WiFi only option)

## Capabilities

### New Capabilities
- `player-media-cache`: ExoPlayer cache setup
- `player-download-manager`: Background downloads
- `player-cache-policy`: LRU + pinning
- `player-checksum-validation`: Integrity checks
- `player-quota-management`: Disk space management

## Impact

- `apps/player-androidtv/src/.../cache/CacheManager.kt`
- `apps/player-androidtv/src/.../cache/DownloadManager.kt`
- `apps/player-androidtv/src/.../cache/CachePolicy.kt`
- `apps/player-androidtv/src/.../cache/ChecksumValidator.kt`
