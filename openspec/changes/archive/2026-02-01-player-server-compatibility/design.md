# Player-Server Compatibility — Design

## Overview

Manages version compatibility between player apps and server API, enabling graceful degradation and forced updates when necessary.

## Server-Side Components

### Version Info (Contracts)

```kotlin
data class VersionInfo(
    val serverVersion: String,
    val apiVersion: String,
    val minSupportedPlayerVersion: String,
    val recommendedPlayerVersion: String,
    val forceUpdateRequired: Boolean,
    val forceUpdateDeadline: Instant?,
    val deprecationWarnings: List<String>
)
```

### Feature Flags

```kotlin
data class FeatureFlags(
    val flags: Map<String, Boolean>
)

// Default flags
object DefaultFeatures {
    val SSE_OVERLAY = "sseOverlay" to true
    val ENHANCED_CACHE = "enhancedCache" to false
}
```

### Config Response Enhancement

The `/api/player/config` endpoint includes version info:

```json
{
  "channelId": "uuid",
  "serverVersion": "1.0.0",
  "apiVersion": "v1",
  "minSupportedPlayerVersion": "1.0.0",
  "recommendedPlayerVersion": "1.0.0",
  "forceUpdateRequired": false,
  "forceUpdateDeadline": null,
  "features": {
    "sseOverlay": true,
    "enhancedCache": false
  },
  "deprecationWarnings": []
}
```

### Compatibility Service

Checks if player version is compatible:
- Parse player version from User-Agent or header
- Compare against min supported version
- Determine if update is recommended
- Check force update requirements

## Components to Create

### Contracts Layer
- `VersionInfo.kt` - Version info data classes
- `FeatureFlags.kt` - Feature flag definitions

### Server Layer
- `CompatibilityService.kt` - Version checking logic
- Update device config routes to include version info

## Security

- Version info is read-only from player perspective
- Force update flag controlled by admin/ops
- Feature flags can be tenant-scoped (future)

## Scope

Phase 1 (this implementation):
- Add version info to contracts
- Add CompatibilityService with basic version check
- Include version info in player config response

Phase 2 (future):
- Per-tenant feature flags
- Force update deadline enforcement
- Admin UI for version management
