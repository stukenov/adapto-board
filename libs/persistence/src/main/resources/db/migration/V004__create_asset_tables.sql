-- Assets table
CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    type asset_type NOT NULL,
    name VARCHAR(255) NOT NULL,
    status asset_status NOT NULL DEFAULT 'UPLOADING',
    duration_ms INTEGER, -- NULL for images
    mime_type VARCHAR(100) NOT NULL,
    checksum_sha256 VARCHAR(64),
    storage_key VARCHAR(500) NOT NULL,
    width INTEGER,
    height INTEGER,
    file_size_bytes BIGINT,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Asset versions table (for transcoded versions)
CREATE TABLE asset_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    profile asset_profile NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    codec VARCHAR(50),
    bitrate INTEGER,
    container VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_asset_versions_asset_profile UNIQUE (asset_id, profile)
);
