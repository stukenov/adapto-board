# Player Status Screen — Proposal

## Why

Status screen — диагностика для IT и support. Должен давать всю информацию для triage без подключения к админке.

## What Changes

### Status Screen Access
- Hidden shortcut (e.g., long press on remote button)
- PIN protection (optional)
- Auto-hide after timeout

### Status Information

#### Device Info
- Device ID
- Model
- Android version
- App version

#### Network Status
- Online/offline
- Network type (WiFi/Ethernet)
- IP address
- Last successful request

#### Config Status
- Last config fetch time
- Config version
- Assigned channel (name, id)
- Schedule version

#### Playlist Status
- Last playlist fetch time
- Items count
- Cached items count
- Current item

#### Playback Status
- Current asset (name, id)
- Playback state (playing, paused, error)
- Position / duration

#### SSE Status
- Connected / disconnected
- Last event time
- Overlay version

#### Cache Status
- Used space / quota
- Cached assets count
- Download queue

#### Errors
- Last N errors (timestamp, code, message)
- Expandable details

### Actions

#### Export Logs
- Create log file
- Device ID in filename
- Instructions for IT

#### Support Bundle
- Copy to clipboard
- QR code with minimal info

#### Force Refresh
- Config refresh
- Playlist refresh
- SSE reconnect

### UI
- Grid layout for sections
- Color coding (green/yellow/red)
- Scrollable
- Remote-friendly navigation

## Capabilities

### New Capabilities
- `player-status-screen`: Diagnostics UI
- `player-status-shortcut`: Hidden access
- `player-log-export`: Log file generation
- `player-support-bundle`: Quick diagnostics

## Impact

- `apps/player-androidtv/src/.../status/StatusActivity.kt`
- `apps/player-androidtv/src/.../status/ui/*.kt`
- `apps/player-androidtv/src/.../status/LogExporter.kt`
- `apps/player-androidtv/src/.../status/SupportBundle.kt`
