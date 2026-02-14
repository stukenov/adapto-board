-- Add voiceover support to schedule items and assets
ALTER TABLE schedule_items ADD COLUMN voiceover_key VARCHAR(500);
ALTER TABLE assets ADD COLUMN voiceover_key VARCHAR(500);
