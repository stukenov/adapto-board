-- Story groups for organizing schedule items
CREATE TABLE story_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    schedule_version_id UUID NOT NULL REFERENCES schedule_versions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_story_groups_tenant ON story_groups(tenant_id);
CREATE INDEX idx_story_groups_version ON story_groups(schedule_version_id);

ALTER TABLE schedule_items ADD COLUMN story_group_id UUID REFERENCES story_groups(id) ON DELETE SET NULL;
