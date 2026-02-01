-- Overlay profiles table
CREATE TABLE overlay_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    definition_json JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_overlay_profiles_tenant_name UNIQUE (tenant_id, name)
);

-- Add FK from channels to overlay_profiles
ALTER TABLE channels
    ADD CONSTRAINT fk_channels_overlay_profile
    FOREIGN KEY (default_overlay_profile_id) REFERENCES overlay_profiles(id) ON DELETE SET NULL;

-- Overlay bindings table
CREATE TABLE overlay_bindings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    overlay_profile_id UUID NOT NULL REFERENCES overlay_profiles(id) ON DELETE CASCADE,
    source_type overlay_source_type NOT NULL,
    source_config_json JSONB NOT NULL DEFAULT '{}',
    status binding_status NOT NULL DEFAULT 'ACTIVE',
    webhook_secret VARCHAR(255), -- for WEBHOOK source type
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Overlay states table (current state per channel)
CREATE TABLE overlay_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    state_json JSONB NOT NULL DEFAULT '{}',
    version BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_overlay_states_tenant_channel UNIQUE (tenant_id, channel_id)
);
