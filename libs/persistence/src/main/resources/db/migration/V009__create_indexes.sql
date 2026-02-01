-- Indexes for users table
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);

-- Indexes for devices table
CREATE INDEX idx_devices_tenant ON devices(tenant_id);
CREATE INDEX idx_devices_tenant_last_seen ON devices(tenant_id, last_seen_at DESC);
CREATE INDEX idx_devices_tenant_channel ON devices(tenant_id, assigned_channel_id);
CREATE INDEX idx_devices_enroll_status ON devices(tenant_id, enroll_status);

-- Indexes for device_groups
CREATE INDEX idx_device_groups_tenant ON device_groups(tenant_id);

-- Indexes for assets table
CREATE INDEX idx_assets_tenant ON assets(tenant_id);
CREATE INDEX idx_assets_tenant_status ON assets(tenant_id, status);
CREATE INDEX idx_assets_tenant_type ON assets(tenant_id, type);
CREATE INDEX idx_assets_tenant_created ON assets(tenant_id, created_at DESC);

-- Indexes for channels table
CREATE INDEX idx_channels_tenant ON channels(tenant_id);

-- Indexes for schedule_versions table
CREATE INDEX idx_schedule_versions_tenant ON schedule_versions(tenant_id);
CREATE INDEX idx_schedule_versions_channel ON schedule_versions(tenant_id, channel_id);
CREATE INDEX idx_schedule_versions_channel_state ON schedule_versions(tenant_id, channel_id, state);

-- Indexes for schedule_items table
CREATE INDEX idx_schedule_items_tenant ON schedule_items(tenant_id);
CREATE INDEX idx_schedule_items_version ON schedule_items(schedule_version_id);

-- Indexes for overlay tables
CREATE INDEX idx_overlay_profiles_tenant ON overlay_profiles(tenant_id);
CREATE INDEX idx_overlay_bindings_tenant ON overlay_bindings(tenant_id);
CREATE INDEX idx_overlay_bindings_channel ON overlay_bindings(tenant_id, channel_id);
CREATE INDEX idx_overlay_states_tenant ON overlay_states(tenant_id);

-- Indexes for audit_log table
CREATE INDEX idx_audit_log_tenant ON audit_log(tenant_id);
CREATE INDEX idx_audit_log_tenant_created ON audit_log(tenant_id, created_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);

-- Indexes for asrun_events table
CREATE INDEX idx_asrun_tenant ON asrun_events(tenant_id);
CREATE INDEX idx_asrun_tenant_device_at ON asrun_events(tenant_id, device_id, at DESC);
CREATE INDEX idx_asrun_tenant_channel_at ON asrun_events(tenant_id, channel_id, at DESC);
CREATE INDEX idx_asrun_at ON asrun_events(at DESC);

-- Indexes for tenant_contacts
CREATE INDEX idx_tenant_contacts_tenant ON tenant_contacts(tenant_id);

-- Indexes for device_actions table
CREATE INDEX idx_device_actions_tenant ON device_actions(tenant_id);
CREATE INDEX idx_device_actions_device ON device_actions(tenant_id, device_id);
CREATE INDEX idx_device_actions_status ON device_actions(tenant_id, status);

-- Indexes for alerts table
CREATE INDEX idx_alerts_tenant ON alerts(tenant_id);
CREATE INDEX idx_alerts_tenant_status ON alerts(tenant_id, status);

-- Indexes for jobs table
CREATE INDEX idx_jobs_status_next_run ON jobs(status, next_run_at);
CREATE INDEX idx_jobs_type_status ON jobs(type, status);
CREATE INDEX idx_jobs_locked ON jobs(locked_by, locked_at) WHERE locked_by IS NOT NULL;
