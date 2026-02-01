# Player-Server Compatibility — Proposal

## Why

Player и server развиваются независимо. Нужна стратегия:
- Backwards compatibility
- Graceful degradation
- Forced updates при critical bugs

## What Changes

### Version Tracking
- Server API version: v1, v2, ...
- Player app version: semantic (1.0.0, 1.1.0, ...)
- Protocol version: в contracts

### Backwards Compatibility
- Server поддерживает N-1 версии Player API
- Deprecation notice за 30 дней
- Breaking changes только в major versions

### Config Response Versioning
`/api/player/config` включает:
```json
{
  ...,
  "serverVersion": "1.2.0",
  "minSupportedPlayerVersion": "1.0.0",
  "recommendedPlayerVersion": "1.1.0",
  "forceUpdateRequired": false,
  "forceUpdateDeadline": null
}
```

### Player Behavior

#### Unknown Fields
- Ignore unknown fields in responses
- Don't fail on extra data

#### Missing Optional Fields
- Use defaults
- Log warning

#### Schema Mismatch
- Validate critical fields
- Fallback to last valid config on parse error

### Force Update Flow
1. Server sets `forceUpdateRequired: true`
2. Player shows "Update required" screen
3. Links to Play Store / MDM
4. Deadline: after deadline, player refuses to play

### Graceful Degradation
- New overlay widget type unknown → render as empty/hidden
- New config field unknown → ignore
- New API endpoint → 404 handled gracefully

### Feature Flags (per tenant)
Server can enable/disable features per tenant:
```json
{
  ...,
  "features": {
    "newOverlayWidgets": false,
    "enhancedCache": true
  }
}
```

### Deprecation Communication
- In config: `deprecationWarnings: ["v1 overlay will be removed in 30 days"]`
- Player logs warnings
- Admin dashboard shows warnings

### Testing
- Contract tests between server and player
- Verify serialization compatibility
- Test with old player versions

## Capabilities

### New Capabilities
- `player-version-check`: Version compatibility check
- `player-force-update`: Mandatory update flow
- `player-feature-flags`: Per-tenant features
- `player-graceful-degradation`: Handle unknowns

## Impact

- `libs/contracts/src/.../VersionInfo.kt`
- `apps/server/src/.../services/CompatibilityService.kt`
- `apps/player-androidtv/src/.../compat/VersionChecker.kt`
- `apps/player-androidtv/src/.../compat/ForceUpdateScreen.kt`
