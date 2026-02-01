# Player Enrollment Flow — Spec

## Requirements

### REQ-ENROLL-001: Enter Code Screen

#### Scenario: Code input UI

- **WHEN** player запускается без enrollment
- **THEN** отображается экран ввода кода
- **AND** большое поле ввода для 6-8 символов
- **AND** виртуальная клавиатура для TV remote
- **AND** кнопка Submit

#### Scenario: Device info display

- **WHEN** экран ввода кода отображается
- **THEN** видна информация об устройстве:
  - Model
  - Android version
  - App version
  - Network status (connected/disconnected)

### REQ-ENROLL-002: Enrollment Process

#### Scenario: Enrolling state

- **WHEN** user нажимает Submit с валидным кодом
- **THEN** отображается "Connecting to server..."
- **AND** progress indicator
- **AND** device info остаётся видимым

#### Scenario: Successful enrollment with channel

- **WHEN** enrollment успешен
- **AND** channel назначен (через code binding)
- **THEN** отображается "Success! Starting playback..."
- **AND** показывается название канала
- **AND** через 2-3 секунды переход к playback

#### Scenario: Successful enrollment without channel

- **WHEN** enrollment успешен
- **AND** channel не назначен
- **THEN** отображается "Waiting for channel assignment"
- **AND** device ID виден (для reference)
- **AND** нейтральная waiting анимация
- **AND** QR code для быстрой привязки в админке

### REQ-ENROLL-003: Error Handling

#### Scenario: Code expired

- **WHEN** код истёк
- **THEN** отображается ошибка "Code expired"
- **AND** "Please request a new code from administrator"
- **AND** кнопка "Try again"

#### Scenario: Code already used

- **WHEN** код уже использован
- **THEN** отображается ошибка "Code already used"
- **AND** инструкция запросить новый код

#### Scenario: Network error

- **WHEN** сеть недоступна
- **THEN** отображается "No network connection"
- **AND** retry с backoff
- **AND** статус попыток виден

#### Scenario: Server error

- **WHEN** сервер возвращает 5xx
- **THEN** отображается "Server unavailable"
- **AND** auto-retry с backoff
- **AND** countdown до следующей попытки

### REQ-ENROLL-004: Token Storage

#### Scenario: Secure token storage

- **WHEN** enrollment успешен
- **THEN** refresh token сохраняется в Android Keystore
- **AND** token encrypted

### REQ-ENROLL-005: Already Enrolled

#### Scenario: Detect existing enrollment

- **WHEN** player запускается
- **AND** valid token существует
- **THEN** пропускается enrollment экран
- **AND** сразу к config polling / playback

#### Scenario: Token invalid

- **WHEN** stored token invalid (revoked/expired)
- **THEN** показывается enrollment экран
- **AND** старый token удаляется
