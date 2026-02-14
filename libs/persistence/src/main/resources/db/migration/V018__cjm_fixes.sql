-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Email verification tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Onboarding state persistence
CREATE TABLE IF NOT EXISTS onboarding_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    current_step VARCHAR(50) NOT NULL DEFAULT 'tenant-settings',
    completed_steps TEXT NOT NULL DEFAULT '',
    skipped_steps TEXT NOT NULL DEFAULT '',
    created_asset_id UUID NULL,
    created_channel_id UUID NULL,
    enroll_code VARCHAR(10) NULL,
    content_policies JSONB NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- User must_change_password flag
ALTER TABLE users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Device logs
CREATE TABLE IF NOT EXISTS device_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    stack_trace TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Device screenshots
CREATE TABLE IF NOT EXISTS device_screenshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    storage_key VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Embed analytics
CREATE TABLE IF NOT EXISTS embed_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id UUID NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    referer VARCHAR(1000) NULL,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Scheduled reports
CREATE TABLE IF NOT EXISTS scheduled_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    filters_json JSONB NULL,
    format VARCHAR(10) NOT NULL DEFAULT 'csv',
    schedule VARCHAR(50) NOT NULL,
    recipients TEXT NOT NULL DEFAULT '',
    created_by UUID NOT NULL REFERENCES users(id),
    last_run TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Asset enhancements
ALTER TABLE assets ADD COLUMN IF NOT EXISTS description TEXT NULL;
ALTER TABLE assets ADD COLUMN IF NOT EXISTS tags TEXT NULL;
ALTER TABLE assets ADD COLUMN IF NOT EXISTS thumbnail_storage_key VARCHAR(500) NULL;

-- Webhook retry count
ALTER TABLE webhook_logs ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_token ON email_verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_device_logs_device ON device_logs(device_id);
CREATE INDEX IF NOT EXISTS idx_device_logs_created ON device_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_device_screenshots_device ON device_screenshots(device_id);
CREATE INDEX IF NOT EXISTS idx_embed_views_channel ON embed_views(channel_id);
CREATE INDEX IF NOT EXISTS idx_embed_views_viewed ON embed_views(viewed_at);
CREATE INDEX IF NOT EXISTS idx_scheduled_reports_tenant ON scheduled_reports(tenant_id);
