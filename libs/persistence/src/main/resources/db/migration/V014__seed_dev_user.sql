-- Seed development user (only for dev environment)
-- Password: admin (bcrypt hash with cost 12)

-- Create default tenant
INSERT INTO tenants (id, name, status, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Demo Tenant',
    'ACTIVE',
    NOW()
) ON CONFLICT DO NOTHING;

-- Create admin user
-- Password: admin
INSERT INTO users (id, tenant_id, email, display_name, status, password_hash, created_at)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'admin@example.com',
    'Admin User',
    'ACTIVE',
    '$2a$12$tulJIJZWsDPlt0FWF8FiU.B4CJGkndUbZLUJhS.Y2w96/h8pCUbny',
    NOW()
) ON CONFLICT DO NOTHING;

-- Assign admin role
INSERT INTO user_roles (user_id, role)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'TENANT_ADMIN'
) ON CONFLICT DO NOTHING;
