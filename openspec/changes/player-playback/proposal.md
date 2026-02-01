# Player Playback — Proposal

## Why

Playback — core функция. Инвариант: "всегда есть что показать". Никогда чёрный экран, даже при ошибках.

## What Changes

### ExoPlayer Setup
- Media3 ExoPlayer
- Video surface (SurfaceView)
- Audio handling
- Codec support validation

### Playback Loop
- Seamless playlist loop
- Smooth transitions between items
- Gapless playback (if possible)

### Error Recovery
- Skip failed asset → next item
- Log error
- Continue playback
- Never stop loop

### Fallback Hierarchy
1. Current cached playlist
2. Last known good asset
3. Fallback screen (local image/animation)

### Playback State
- Current asset tracking
- Position tracking
- Play/pause state
- Error state

### Network Resilience
- Continue from cache when offline
- Resume downloads when online
- No playback interruption

### Asset Transitions
- Crossfade (optional)
- Immediate switch
- Handle different aspect ratios

### Performance
- Hardware acceleration
- Memory management
- Battery optimization (always plugged)

### Playback Events
- Asset start/end events
- Error events
- Position updates
- For as-run reporting

## Capabilities

### New Capabilities
- `player-exoplayer-setup`: ExoPlayer configuration
- `player-playback-loop`: Continuous playlist playback
- `player-error-recovery`: Skip and continue on errors
- `player-fallback-screen`: Never black screen
- `player-playback-events`: Event emission for telemetry

## Impact

- `apps/player-androidtv/src/.../playback/PlaybackManager.kt`
- `apps/player-androidtv/src/.../playback/ExoPlayerFactory.kt`
- `apps/player-androidtv/src/.../playback/PlaybackLoop.kt`
- `apps/player-androidtv/src/.../playback/FallbackScreen.kt`
