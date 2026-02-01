# Player Kiosk Mode — Proposal

## Why

Android TV player должен работать в режиме kiosk 24/7:
- Автозапуск после перезагрузки
- Ограничение выхода из приложения
- Устойчивость к случайным нажатиям
- Работа без пользовательского взаимодействия

## What Changes

### Auto-start
- BroadcastReceiver для BOOT_COMPLETED
- Foreground service для устойчивости
- Запуск playback сразу после boot
- Восстановление состояния после crash

### Kiosk Lock
- Блокировка Home button (если разрешено MDM)
- Блокировка Back button (или переопределение)
- Блокировка Settings access
- PIN для выхода из kiosk (optional)

### Power Management
- Keep screen on (WAKE_LOCK)
- Prevent sleep
- Handle HDMI-CEC events (optional)

### Recovery
- Watchdog для перезапуска при hang
- Auto-restart после crash
- Graceful degradation при ошибках

### MDM Integration
- Managed configurations support
- Remote lock/unlock
- Policy compliance

## Capabilities

### New Capabilities
- `player-auto-start`: Автозапуск после boot
- `player-kiosk-lock`: Блокировка выхода
- `player-power-management`: Screen always on
- `player-crash-recovery`: Автовосстановление

## Impact

- `apps/player-androidtv/src/.../kiosk/KioskManager.kt`
- `apps/player-androidtv/src/.../kiosk/BootReceiver.kt`
- `apps/player-androidtv/src/.../kiosk/WatchdogService.kt`
- `AndroidManifest.xml` — permissions и receivers
