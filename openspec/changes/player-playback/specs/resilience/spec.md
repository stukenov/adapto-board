# Player Playback Resilience — Spec

## Requirements

### REQ-PLAY-001: Never Black Screen

#### Scenario: Always something to show

- **WHEN** player запущен
- **THEN** всегда отображается контент:
  1. Текущий кэшированный playlist
  2. Last known good asset
  3. Fallback screen (локальная картинка)
- **AND** экран никогда не остаётся чёрным

### REQ-PLAY-002: Network Resilience

#### Scenario: Network disconnected during playback

- **WHEN** сеть пропадает во время воспроизведения
- **THEN** playback продолжается из cache
- **AND** отображается весь кэшированный playlist
- **AND** нет прерывания воспроизведения

#### Scenario: Network restored

- **WHEN** сеть восстанавливается
- **THEN** player возобновляет downloads
- **AND** обновляет config/playlist
- **AND** без прерывания текущего playback

### REQ-PLAY-003: Asset Error Recovery

#### Scenario: Corrupted asset

- **WHEN** asset не может быть воспроизведён (corrupted/incompatible)
- **THEN** player пропускает item
- **AND** переходит к следующему item
- **AND** логирует ошибку
- **AND** отправляет error в heartbeat

#### Scenario: Missing asset

- **WHEN** asset отсутствует в cache и недоступен online
- **THEN** player пропускает item
- **AND** продолжает с следующего

### REQ-PLAY-004: Empty Playlist Handling

#### Scenario: Empty playlist received

- **WHEN** server возвращает пустой playlist
- **THEN** player остаётся на предыдущей published версии
- **AND** не переключается на пустой

#### Scenario: All items invalid

- **WHEN** все items в playlist invalid/unavailable
- **THEN** player показывает fallback screen
- **AND** логирует критическую ошибку

### REQ-PLAY-005: Playback Loop

#### Scenario: Continuous loop

- **WHEN** playlist завершается
- **THEN** player начинает сначала
- **AND** loop бесконечный

#### Scenario: Seamless transitions

- **WHEN** один item заканчивается
- **THEN** следующий начинается без pause
- **AND** минимальный gap между items

### REQ-PLAY-006: Fallback Hierarchy

#### Scenario: Fallback order

- **WHEN** основной контент недоступен
- **THEN** player пробует в порядке:
  1. Кэшированный playlist текущей версии
  2. Кэшированный playlist предыдущей версии
  3. Любой кэшированный working asset
  4. Server-provided fallback asset
  5. Local embedded fallback (картинка/анимация)

### REQ-PLAY-007: Playback Events

#### Scenario: Asset start event

- **WHEN** asset начинает воспроизведение
- **THEN** генерируется START event
- **AND** включает: assetId, timestamp, scheduleVersionId

#### Scenario: Asset end event

- **WHEN** asset завершает воспроизведение
- **THEN** генерируется END event
- **AND** включает: duration played

#### Scenario: Error event

- **WHEN** происходит playback error
- **THEN** генерируется ERROR event
- **AND** включает: error code, message, assetId
