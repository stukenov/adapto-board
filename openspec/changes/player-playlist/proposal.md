# Player Playlist Management — Proposal

## Why

Playlist fetch и management — player должен знать какие assets играть, в каком порядке, с какими time windows.

## What Changes

### Playlist Fetch
- `GET /api/player/playlist`
- Parse manifest
- Validate response

### Playlist Model
- scheduleVersionId
- items: assetId, url, checksum, durationMs, orderIndex
- fallback item info
- time windows (valid_from, valid_to, days_of_week, time_start/end)

### Playlist Storage
- Persist current playlist
- Track applied version
- Playlist history (last N)

### Playlist Calculation
- Filter items by current time/day
- Sort by orderIndex
- Handle empty playlist (fallback)

### Asset URL Handling
- Signed URL parsing
- URL expiry tracking
- Re-fetch manifest when URLs expire

### Version Tracking
- Compare with current playing version
- Detect updates
- Seamless transition to new version

### Fallback Playlist
- Server-provided fallback
- Local fallback asset
- Never empty playlist

## Capabilities

### New Capabilities
- `player-playlist-fetch`: Manifest fetch
- `player-playlist-storage`: Playlist persistence
- `player-playlist-calculation`: Active items calculation
- `player-playlist-urls`: URL management

## Impact

- `apps/player-androidtv/src/.../playlist/PlaylistManager.kt`
- `apps/player-androidtv/src/.../playlist/PlaylistStorage.kt`
- `apps/player-androidtv/src/.../playlist/PlaylistModels.kt`
- `apps/player-androidtv/src/.../playlist/TimeWindowCalculator.kt`
