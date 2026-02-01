# Player Enrollment Flow — Proposal

## Why

Enrollment — первый контакт устройства с системой. UX должен быть простым для IT: ввести код → устройство появляется в админке.

## What Changes

### Enrollment UI

#### Enter Code Screen
- Large code input (6-8 characters)
- Virtual keyboard for TV remote
- Device info display:
  - Model
  - Android version
  - App version
  - Network status
- Submit button

#### Enrolling Screen
- Progress indicator
- "Connecting to server..."
- Device info visible

#### Success Screens

##### Assigned to Channel
- "Success! Starting playback..."
- Channel name
- Auto-transition to playback

##### Not Assigned
- "Waiting for channel assignment"
- Device ID display
- QR code with device info (for quick admin action)
- Neutral waiting animation

### Error Handling
- Code expired → "Code expired. Request new code."
- Code already used → "Code already used."
- Network error → Retry with backoff
- Server error → "Server unavailable. Retrying..."

### Token Storage
- Receive refresh token on success
- Store securely (Android Keystore)
- Handle enrollment revocation

### Re-enrollment
- "Already enrolled" detection
- Factory reset flow hint
- Support contact info

### QR Code (optional)
- Display QR with enrollCode + URL
- For quick binding in admin

## Capabilities

### New Capabilities
- `player-enroll-ui`: Enrollment screens
- `player-enroll-api`: Enrollment API call
- `player-enroll-token-storage`: Secure token storage
- `player-enroll-error-handling`: Error states

## Impact

- `apps/player-androidtv/src/.../enrollment/EnrollmentActivity.kt`
- `apps/player-androidtv/src/.../enrollment/ui/*.kt`
- `apps/player-androidtv/src/.../enrollment/EnrollmentManager.kt`
