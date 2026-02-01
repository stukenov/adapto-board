-- Audit log table
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL, -- NULL for system actions
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_type actor_type NOT NULL,
    action VARCHAR(100) NOT NULL, -- e.g., 'create', 'update', 'delete', 'publish'
    entity_type VARCHAR(100) NOT NULL, -- e.g., 'channel', 'asset', 'schedule'
    entity_id UUID NOT NULL,
    diff_json JSONB, -- before/after or change details
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- As-run events table (playback telemetry)
CREATE TABLE asrun_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    channel_id UUID REFERENCES channels(id) ON DELETE SET NULL,
    schedule_version_id UUID REFERENCES schedule_versions(id) ON DELETE SET NULL,
    asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    event_type asrun_event_type NOT NULL,
    at TIMESTAMPTZ NOT NULL, -- event timestamp
    details_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
