# Admin Web Onboarding Wizard — Proposal

## Why

"Pilot setup wizard" в UX blueprint — критичен для time-to-first-value. Новый tenant должен за 1-2 часа:
- Настроить tenant
- Загрузить первый контент
- Подключить первое устройство
- Увидеть контент на экране

Без wizard пользователь теряется в меню.

## What Changes

### Wizard Flow

#### Step 1: Tenant Settings
- Timezone
- Company name
- Offline threshold

#### Step 2: Content Policies
- Allowed codecs (preset: H.264/MP4)
- Max bitrate
- Max resolution
- Max file size

#### Step 3: First Asset Upload
- Drag & drop upload
- Progress indication
- Sample content suggestion

#### Step 4: First Channel
- Channel name
- Add uploaded asset to schedule
- Quick publish

#### Step 5: First Device
- Generate enroll code
- QR code display
- Instructions for Android TV
- "Bind to channel" enabled by default

#### Step 6: Verify
- Wait for device enrollment
- Confirm playback started
- Success celebration

### Wizard UX
- Progress indicator (6 steps)
- Skip individual steps
- "Do later" option
- Contextual help per step

### Re-entry
- Wizard state saved
- Resume from where left off
- "Setup incomplete" badge on Home

### Empty States Integration
- Empty states link to relevant wizard step
- "No channels" → Step 4
- "No devices" → Step 5

## Capabilities

### New Capabilities
- `admin-onboarding-wizard`: Step-by-step setup
- `admin-wizard-progress`: Progress tracking
- `admin-wizard-resume`: Resume incomplete setup

## Impact

- `apps/server/src/.../routes/admin/OnboardingRoutes.kt`
- `apps/server/src/.../views/onboarding/*.kt`
- `libs/persistence/src/.../OnboardingStateRepository.kt`
