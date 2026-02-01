-- Webhook call logs
CREATE TABLE webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    binding_id UUID NOT NULL REFERENCES overlay_bindings(id) ON DELETE CASCADE,
    status_code INT NOT NULL,
    latency_ms INT NOT NULL,
    payload_size INT NOT NULL,
    error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_logs_binding_created ON webhook_logs(binding_id, created_at DESC);
