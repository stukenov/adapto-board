-- Devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    display_name VARCHAR(255) NOT NULL,
    enroll_status device_enroll_status NOT NULL DEFAULT 'PENDING',
    device_secret_hash VARCHAR(255),
    assigned_channel_id UUID, -- FK added after channels table exists
    last_seen_at TIMESTAMPTZ,
    app_version VARCHAR(50),
    android_model VARCHAR(100),
    android_version VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Device groups table
CREATE TABLE device_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_device_groups_tenant_name UNIQUE (tenant_id, name)
);

-- Device group members (many-to-many)
CREATE TABLE device_group_members (
    device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES device_groups(id) ON DELETE CASCADE,
    PRIMARY KEY (device_id, group_id)
);
