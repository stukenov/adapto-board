-- Tenant status
CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'SUSPENDED', 'MAINTENANCE');

-- User status and roles
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');
CREATE TYPE user_role AS ENUM ('TENANT_ADMIN', 'OPERATOR', 'SUPPORT_AGENT', 'SUPPORT_ADMIN');

-- Device status
CREATE TYPE device_enroll_status AS ENUM ('PENDING', 'ENROLLED', 'REJECTED');

-- Asset types and status
CREATE TYPE asset_type AS ENUM ('VIDEO', 'IMAGE');
CREATE TYPE asset_status AS ENUM ('UPLOADING', 'PROCESSING', 'READY', 'REJECTED', 'ARCHIVED');
CREATE TYPE asset_profile AS ENUM ('ORIGINAL', 'NORMALIZED');

-- Channel and schedule
CREATE TYPE channel_status AS ENUM ('ACTIVE', 'PAUSED');
CREATE TYPE schedule_state AS ENUM ('DRAFT', 'PUBLISHED', 'ROLLED_BACK');

-- Overlay
CREATE TYPE binding_status AS ENUM ('ACTIVE', 'PAUSED');
CREATE TYPE overlay_source_type AS ENUM ('REST_PULL', 'WEBHOOK');

-- As-run events
CREATE TYPE asrun_event_type AS ENUM ('PLAY_START', 'PLAY_END', 'SKIP', 'ERROR');

-- Device actions
CREATE TYPE action_type AS ENUM ('FORCE_CONFIG_REFRESH', 'FORCE_PLAYLIST_REFRESH', 'ROTATE_DEVICE_TOKEN');
CREATE TYPE action_status AS ENUM ('PENDING', 'ACKED', 'FAILED', 'EXPIRED');

-- Alerts
CREATE TYPE alert_type AS ENUM ('DEVICE_OFFLINE', 'PLAYBACK_ERROR', 'STORAGE_QUOTA');
CREATE TYPE alert_status AS ENUM ('OPEN', 'ACKNOWLEDGED', 'RESOLVED');

-- Jobs
CREATE TYPE job_status AS ENUM ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED');

-- Contacts
CREATE TYPE contact_type AS ENUM ('TECHNICAL', 'BUSINESS', 'BILLING');

-- Actor type for audit
CREATE TYPE actor_type AS ENUM ('USER', 'DEVICE', 'SYSTEM');
