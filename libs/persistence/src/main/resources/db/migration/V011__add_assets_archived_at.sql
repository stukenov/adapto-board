-- Add archived_at timestamp for soft delete
ALTER TABLE assets ADD COLUMN archived_at TIMESTAMPTZ;

-- Add rejection_reason for validation failures
ALTER TABLE assets ADD COLUMN rejection_reason TEXT;

-- Index for finding archived assets (cleanup job)
CREATE INDEX idx_assets_archived_at ON assets(archived_at) WHERE archived_at IS NOT NULL;

-- Index for finding assets by status
CREATE INDEX idx_assets_status ON assets(tenant_id, status);
