-- Add maintenance mode fields to tenants
ALTER TABLE tenants ADD COLUMN support_tier VARCHAR(20) NOT NULL DEFAULT 'BASIC';
ALTER TABLE tenants ADD COLUMN release_ring VARCHAR(20) NOT NULL DEFAULT 'STABLE';
ALTER TABLE tenants ADD COLUMN maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tenants ADD COLUMN maintenance_reason TEXT;
ALTER TABLE tenants ADD COLUMN maintenance_until TIMESTAMP WITH TIME ZONE;
