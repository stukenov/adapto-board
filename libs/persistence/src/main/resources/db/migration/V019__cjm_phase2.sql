-- Content approval workflow
ALTER TABLE assets ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE assets ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ NULL;
ALTER TABLE assets ADD COLUMN IF NOT EXISTS checksum VARCHAR(64) NULL;

-- Device location & power schedule
ALTER TABLE devices ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION NULL;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION NULL;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS location_name VARCHAR(255) NULL;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS power_on_time VARCHAR(5) NULL;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS power_off_time VARCHAR(5) NULL;

-- Notification preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email_on_device_offline BOOLEAN NOT NULL DEFAULT TRUE,
    email_on_alert BOOLEAN NOT NULL DEFAULT TRUE,
    email_on_schedule_publish BOOLEAN NOT NULL DEFAULT FALSE,
    email_daily_digest BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

-- Embed analytics enhancement
ALTER TABLE embed_views ADD COLUMN IF NOT EXISTS duration_seconds INTEGER NULL;
ALTER TABLE embed_views ADD COLUMN IF NOT EXISTS country VARCHAR(2) NULL;

-- User invite expiration
ALTER TABLE users ADD COLUMN IF NOT EXISTS invite_expires_at TIMESTAMPTZ NULL;

-- Overlay scheduling
ALTER TABLE overlay_bindings ADD COLUMN IF NOT EXISTS schedule_start TIMESTAMPTZ NULL;
ALTER TABLE overlay_bindings ADD COLUMN IF NOT EXISTS schedule_end TIMESTAMPTZ NULL;

-- Channel groups
CREATE TABLE IF NOT EXISTS channel_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
ALTER TABLE channels ADD COLUMN IF NOT EXISTS group_id UUID NULL REFERENCES channel_groups(id) ON DELETE SET NULL;

-- Session tracking
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_notification_preferences_user ON notification_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_active ON user_sessions(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_channel_groups_tenant ON channel_groups(tenant_id);
CREATE INDEX IF NOT EXISTS idx_assets_checksum ON assets(checksum) WHERE checksum IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_assets_approval ON assets(approval_status);
CREATE INDEX IF NOT EXISTS idx_assets_expires ON assets(expires_at) WHERE expires_at IS NOT NULL;
