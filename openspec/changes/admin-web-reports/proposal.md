# Admin Web Reports — Proposal

## Why

Reports для IT Owner, compliance и business review. Audit log и as-run — обязательные для B2B.

## What Changes

### Audit Log
- Filters: entity type, entity id, actor, action, time range
- Table: timestamp, actor, action, entity, diff summary
- Diff view modal
- Export CSV button
- Pagination

### As-Run Reports
- Filters: device, channel, time range
- Timeline view (visual)
- Events table: timestamp, asset, event type, duration
- Summary: total play time, unique assets, gaps
- "Unknown gaps" highlight для offline periods
- Export CSV/PDF

### Pilot Scorecard (Dashboard)
- KPI cards:
  - Publish P50/P95
  - Uptime %
  - Online rate %
  - Overlay latency P95
- Trends charts
- Date range selector
- Export report

### Report Templates
- Pilot summary report (PDF)
- Weekly status report
- Device health report

## Capabilities

### New Capabilities
- `admin-audit-log-view`: Просмотр audit log
- `admin-asrun-reports`: As-run отчёты
- `admin-pilot-scorecard`: KPI dashboard
- `admin-reports-export`: Export в CSV/PDF

## Impact

- `apps/server/src/.../routes/admin/ReportRoutes.kt`
- `apps/server/src/.../views/reports/*.kt`
