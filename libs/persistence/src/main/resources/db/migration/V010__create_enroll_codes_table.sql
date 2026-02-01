-- Enroll codes table for device enrollment
CREATE TYPE enroll_code_status AS ENUM ('PENDING', 'USED', 'EXPIRED');

CREATE TABLE enroll_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    code VARCHAR(10) NOT NULL,
    status enroll_code_status NOT NULL DEFAULT 'PENDING',
    channel_id UUID REFERENCES channels(id) ON DELETE SET NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    used_by_device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_enroll_codes_code UNIQUE (code)
);

CREATE INDEX idx_enroll_codes_tenant ON enroll_codes(tenant_id);
CREATE INDEX idx_enroll_codes_status ON enroll_codes(status);

-- Add revoked_at to devices
ALTER TABLE devices ADD COLUMN revoked_at TIMESTAMPTZ;
