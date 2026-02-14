-- Add AUDIO and SLIDESHOW asset types
ALTER TYPE asset_type ADD VALUE IF NOT EXISTS 'AUDIO';
ALTER TYPE asset_type ADD VALUE IF NOT EXISTS 'SLIDESHOW';

-- Add slideshow definition column (JSONB) to assets table
ALTER TABLE assets ADD COLUMN IF NOT EXISTS slideshow_definition JSONB NULL;
