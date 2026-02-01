# Player Overlay Rendering — Spec

## Requirements

### REQ-OVUI-001: Widget Types

#### Scenario: Text widget

- **WHEN** overlay содержит text widget
- **THEN** player отображает текст
- **AND** применяет: position, size, font, color, alignment

#### Scenario: Ticker widget

- **WHEN** overlay содержит ticker widget
- **THEN** player отображает scrolling text
- **AND** скорость настраиваемая
- **AND** loop бесконечный

#### Scenario: Table widget

- **WHEN** overlay содержит table widget
- **THEN** player отображает таблицу с N строками
- **AND** поддерживает headers и rows
- **AND** auto-scroll для длинных таблиц

#### Scenario: KPI tiles widget

- **WHEN** overlay содержит KPI widget
- **THEN** player отображает плитки с числами/метриками
- **AND** поддерживает label и value
- **AND** опционально trend indicator

#### Scenario: QR code widget

- **WHEN** overlay содержит QR widget
- **THEN** player генерирует и отображает QR код
- **AND** размер настраиваемый

#### Scenario: Image widget

- **WHEN** overlay содержит image widget
- **THEN** player отображает изображение по URL
- **AND** поддерживает: position, size, opacity

### REQ-OVUI-002: Layout

#### Scenario: Position widgets

- **WHEN** widget имеет position config
- **THEN** widget размещается по: x, y (или anchors: top/bottom/left/right)
- **AND** z-order определяется порядком

#### Scenario: Overlay on top of video

- **WHEN** overlay и video активны
- **THEN** overlay рендерится поверх video
- **AND** video остаётся видимым

### REQ-OVUI-003: State Application

#### Scenario: Apply state

- **WHEN** player получает state event
- **THEN** все widgets пересоздаются/обновляются
- **AND** UI обновляется

#### Scenario: Apply patch

- **WHEN** player получает patch event
- **THEN** только affected widgets обновляются
- **AND** остальные widgets остаются
- **AND** UI обновляется efficiently

### REQ-OVUI-004: Resilience

#### Scenario: SSE disconnected

- **WHEN** SSE соединение разрывается
- **THEN** overlay остаётся на last known state
- **AND** video продолжает воспроизводиться

#### Scenario: Prolonged disconnect

- **WHEN** SSE недоступен дольше threshold
- **THEN** overlay может быть скрыт
- **AND** video продолжает
- **AND** overlay восстанавливается при reconnect

#### Scenario: Unknown widget type

- **WHEN** state содержит unknown widget type
- **THEN** widget игнорируется
- **AND** остальные widgets отображаются
- **AND** error логируется

### REQ-OVUI-005: Performance

#### Scenario: Efficient recomposition

- **WHEN** patch затрагивает один widget
- **THEN** только этот widget перерисовывается
- **AND** нет full screen redraw

#### Scenario: Memory management

- **WHEN** overlay содержит images
- **THEN** images кэшируются
- **AND** memory освобождается при remove
