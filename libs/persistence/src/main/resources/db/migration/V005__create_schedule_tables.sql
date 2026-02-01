-- Channels table
CREATE TABLE channels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    status channel_status NOT NULL DEFAULT 'ACTIVE',
    default_overlay_profile_id UUID, -- FK added after overlay_profiles exists
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_channels_tenant_name UNIQUE (tenant_id, name)
);

-- Add FK from devices to channels
ALTER TABLE devices
    ADD CONSTRAINT fk_devices_channel
    FOREIGN KEY (assigned_channel_id) REFERENCES channels(id) ON DELETE SET NULL;

-- Schedule versions table (immutable)
CREATE TABLE schedule_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    state schedule_state NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMPTZ,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_schedule_versions_tenant_channel_version UNIQUE (tenant_id, channel_id, version)
);

-- Schedule items table
CREATE TABLE schedule_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    schedule_version_id UUID NOT NULL REFERENCES schedule_versions(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    valid_from DATE,
    valid_to DATE,
    days_of_week INTEGER, -- bitmask: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun
    time_start TIME,
    time_end TIME,
    weight INTEGER DEFAULT 1,
    CONSTRAINT uq_schedule_items_version_order UNIQUE (tenant_id, schedule_version_id, order_index)
);
