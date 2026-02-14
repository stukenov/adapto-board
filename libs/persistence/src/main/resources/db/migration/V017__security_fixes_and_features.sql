-- Remove dev seed user from production
DELETE FROM user_roles WHERE user_id = 'b0000000-0000-0000-0000-000000000001';
DELETE FROM users WHERE id = 'b0000000-0000-0000-0000-000000000001' AND email = 'admin@example.com';

-- Add MANUAL to overlay_source_type enum
ALTER TYPE overlay_source_type ADD VALUE IF NOT EXISTS 'MANUAL';

-- Add embed_enabled flag to channels
ALTER TABLE channels ADD COLUMN IF NOT EXISTS embed_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- Add REVOKED to device_enroll_status
ALTER TYPE device_enroll_status ADD VALUE IF NOT EXISTS 'REVOKED';

-- Refresh token storage
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    subject_id UUID NOT NULL,
    subject_type VARCHAR(20) NOT NULL,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_subject ON refresh_tokens(subject_id);

-- API keys
CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    key_hash VARCHAR(128) NOT NULL UNIQUE,
    key_prefix VARCHAR(12) NOT NULL,
    scopes TEXT[] NOT NULL DEFAULT '{}',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_api_keys_tenant ON api_keys(tenant_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_hash ON api_keys(key_hash);
