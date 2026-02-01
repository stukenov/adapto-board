# 05 — Android TV Player (Kotlin)

## 1) Цели приложения

- Надёжно воспроизводить плейлист 24/7 (без “чёрного экрана”).
- Быстро применять изменения расписания.
- Работать в плохих сетях (кэш + оффлайн).
- Показывать overlay поверх видео (SSE data layer).
- Давать поддержку/эксплуатацию: статусы, диагностика, версия, логи ошибок.

## 2) Архитектура приложения (слои)

Рекомендуемый подход: один Android TV APK (kiosk), Kotlin + Coroutines.

- `Network`:
  - Ktor client (или OkHttp) + JSON (kotlinx.serialization)
  - retry/backoff, таймауты
- `Config`:
  - polling `/api/player/config`
  - сохранение последнего рабочего конфига (DataStore)
- `Playlist`:
  - fetch `/api/player/playlist`
  - нормализация “порядок/длительность/URL/хэши”
- `Cache`:
  - MediaCache на диске (ExoPlayer cache)
  - контроль квоты диска + eviction policy
- `Playback`:
  - ExoPlayer (Media3)
  - устойчивый loop, fallback, recovery
- `Overlay`:
  - SSE клиент
  - state+patch reducer
  - рендер поверх видео (Jetpack Compose)
- `Telemetry`:
  - heartbeat
  - as-run events batching

## 3) Поведение воспроизведения (инварианты)

- Всегда есть “что показать”:
  1) текущий кэшированный плейлист,
  2) last known good asset,
  3) fallback screen (локальная картинка/анимация).
- “Сеть пропала” не останавливает показ: проигрывание идёт из кэша.
- “Новый плейлист сломан/пустой” не ломает экран: остаёмся на предыдущей опубликованной версии.
- Ошибка конкретного asset → пропускаем item, логируем, продолжаем.

## 4) Кэширование

### 4.1 Что кэшируем

- Все assets, необходимые для активного `scheduleVersionId`.
- (опционально) следующий scheduleVersion, если backend отдаёт hint.

### 4.2 Контроль диска

- Жёсткая квота (например, 4–16 GB, configurable).
- Политика: LRU + “закрепить” текущую активную версию.
- Валидация: checksum (sha256) после загрузки, чтобы не играть битые файлы.

## 5) Overlay (SSE) — устойчивость

- SSE соединение держим постоянно, но:
  - backoff при обрывах,
  - keepalive таймер,
  - если SSE недоступен — overlay скрывается/замораживается, видео продолжает играть.
- `state+patch`:
  - при разрыве: запрашиваем `state` заново (или сервер сам пошлёт `state` при reconnection).
  - применяем `patch` только если `version` совпадает/последовательна.

## 6) Enrollment UX (Android TV)

Минимум для пилота:

1. Экран “Enter code” (6–8 символов).
2. Показывать устройство info (model, android version, app version).
3. После enroll — экран “assigned/not assigned”:
   - если канал не назначен — показываем нейтральный экран “ожидание назначения”.

Опционально:
- QR, который содержит `enrollCode` и URL для быстрой привязки в админке.

## 7) Kiosk / автостарт / обновления

- Автозапуск после перезагрузки (BroadcastReceiver + foreground service при необходимости).
- Ограничение выхода из приложения (если разрешено политикой устройства/MDM).
- Обновления:
  - v1: ручное (через managed Google Play/MDM).
  - R1: “канареечный rollout” через группы устройств (не внутри приложения).

## 8) Диагностика (must для B2B)

- Экран “Status” (скрытый shortcut):
  - online/offline,
  - последний config/playlist time,
  - текущий asset,
  - версия schedule,
  - SSE status,
  - последние ошибки.
- “Отчёт в поддержку”:
  - экспорт логов (в файл) + device id.

