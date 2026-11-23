-- TaskFlow Identity Service learning-project baseline.
-- This migration represents the complete current schema in one version.

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    CONSTRAINT ck_roles_name CHECK (name IN ('USER', 'SUPPORT', 'SUBADMIN', 'ADMIN'))
);

CREATE TABLE authorities (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    CONSTRAINT ck_authorities_name CHECK (name IN (
        'USER_READ', 'USER_UPDATE', 'USER_DISABLE', 'USER_ENABLE', 'USER_BAN',
        'USER_UNBAN', 'USER_DELETE', 'ROLE_READ', 'ROLE_ASSIGN', 'ROLE_REVOKE',
        'AUDIT_LOG_READ', 'PLATFORM_SETTINGS_READ', 'PLATFORM_SETTINGS_UPDATE'
    ))
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    locked_until TIMESTAMP,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    role_id UUID NOT NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT ck_users_account_status CHECK (account_status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

CREATE TABLE role_authorities (
    role_id UUID NOT NULL,
    authority_id UUID NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    CONSTRAINT fk_role_authorities_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_authorities_authority FOREIGN KEY (authority_id) REFERENCES authorities(id)
);

CREATE TABLE user_permission_overrides (
    user_id UUID NOT NULL,
    authority_id UUID NOT NULL,
    effect VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, authority_id),
    CONSTRAINT fk_user_permission_overrides_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_permission_overrides_authority FOREIGN KEY (authority_id) REFERENCES authorities(id),
    CONSTRAINT ck_user_permission_overrides_effect CHECK (effect IN ('GRANT', 'REVOKE'))
);

CREATE TABLE devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_identifier VARCHAR(255) NOT NULL,
    device_type VARCHAR(32) NOT NULL,
    operating_system VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_devices_device_type CHECK (device_type IN ('DESKTOP', 'LAPTOP', 'TABLET', 'MOBILE'))
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_id UUID,
    ip_address VARCHAR(45) NOT NULL,
    status VARCHAR(32) NOT NULL,
    user_agent VARCHAR(1000),
    browser VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    last_active_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    absolute_expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_sessions_device FOREIGN KEY (device_id) REFERENCES devices(id),
    CONSTRAINT ck_sessions_status CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED')),
    CONSTRAINT ck_sessions_expiry_order CHECK (absolute_expires_at >= expires_at)
);

CREATE TABLE workflow_types (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT ck_workflow_types_code CHECK (code IN (
        'PASSWORD_RECOVERY', 'EMAIL_VERIFICATION', 'MFA', 'EMAIL_CHANGE',
        'PHONE_VERIFICATION', 'TRANSACTION_CONFIRMATION'
    ))
);

CREATE TABLE challenge_mechanism_types (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT ck_challenge_mechanism_types_code CHECK (
        code IN ('OTP', 'MAGIC_LINK', 'PASSKEY', 'AUTHENTICATOR_APP')
    )
);

CREATE TABLE workflow_mechanism_policies (
    id UUID PRIMARY KEY,
    workflow_type_id UUID NOT NULL,
    mechanism_type_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority SMALLINT NOT NULL DEFAULT 0,
    config JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT uk_workflow_mechanism_policy UNIQUE (workflow_type_id, mechanism_type_id),
    CONSTRAINT fk_policy_workflow_type FOREIGN KEY (workflow_type_id) REFERENCES workflow_types(id),
    CONSTRAINT fk_policy_mechanism_type FOREIGN KEY (mechanism_type_id) REFERENCES challenge_mechanism_types(id)
);

CREATE TABLE security_challenges (
    id UUID PRIMARY KEY,
    workflow_mechanism_policy_id UUID NOT NULL,
    user_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    subject_context JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    CONSTRAINT fk_security_challenges_policy FOREIGN KEY (workflow_mechanism_policy_id)
        REFERENCES workflow_mechanism_policies(id),
    CONSTRAINT fk_security_challenges_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_security_challenges_status CHECK (
        status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT ck_security_challenges_attempt_count CHECK (
        attempt_count >= 0 AND max_attempts > 0 AND attempt_count <= max_attempts
    ),
    CONSTRAINT ck_security_challenges_expiry CHECK (expires_at >= created_at),
    CONSTRAINT ck_security_challenges_verified_at CHECK (
        (status = 'VERIFIED' AND verified_at IS NOT NULL)
        OR (status <> 'VERIFIED' AND verified_at IS NULL)
    )
);

CREATE TABLE password_challenges (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_password_challenges_security_challenge FOREIGN KEY (id)
        REFERENCES security_challenges(id)
);

CREATE TABLE otp_challenges (
    id UUID PRIMARY KEY,
    code_hash VARCHAR(255) NOT NULL,
    delivery_channel VARCHAR(32) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    CONSTRAINT fk_otp_challenges_security_challenge FOREIGN KEY (id)
        REFERENCES security_challenges(id),
    CONSTRAINT ck_otp_challenges_delivery_channel CHECK (
        delivery_channel IN ('EMAIL')
    )
);

CREATE TABLE authentication_attempts (
    id UUID PRIMARY KEY,
    user_id UUID,
    attempted_identifier VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(64),
    security_challenge_id UUID,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(1000),
    attempt_count INTEGER NOT NULL DEFAULT 0,
    attempted_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_authentication_attempts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_authentication_attempts_challenge FOREIGN KEY (security_challenge_id)
        REFERENCES security_challenges(id),
    CONSTRAINT ck_authentication_attempts_status CHECK (status IN ('SUCCESS', 'FAILURE')),
    CONSTRAINT ck_authentication_attempts_count CHECK (attempt_count >= 0),
    CONSTRAINT ck_authentication_attempts_failure_reason CHECK (
        failure_reason IS NULL OR failure_reason IN (
            'INVALID_CREDENTIALS', 'ACCOUNT_LOCKED', 'ACCOUNT_DISABLED',
            'EMAIL_NOT_VERIFIED', 'CREDENTIALS_EXPIRED'
        )
    )
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    session_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    revoked_at TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_refresh_tokens_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT ck_refresh_tokens_expiry CHECK (expires_at >= created_at),
    CONSTRAINT ck_refresh_tokens_state CHECK (used_at IS NULL OR used_at >= created_at)
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_status_locked_until ON users(account_status, locked_until);
CREATE INDEX idx_role_authorities_authority_id ON role_authorities(authority_id);
CREATE INDEX idx_user_permission_overrides_authority_id ON user_permission_overrides(authority_id);
CREATE INDEX idx_devices_user_id ON devices(user_id);
CREATE UNIQUE INDEX uk_devices_active_identifier
    ON devices(user_id, device_identifier)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_sessions_user_status ON sessions(user_id, status);
CREATE INDEX idx_sessions_status_expiry ON sessions(status, expires_at);
CREATE INDEX idx_sessions_device_id ON sessions(device_id);
CREATE INDEX idx_workflow_policies_workflow_type ON workflow_mechanism_policies(workflow_type_id);
CREATE INDEX idx_workflow_policies_mechanism_type ON workflow_mechanism_policies(mechanism_type_id);
CREATE INDEX idx_security_challenges_user_status ON security_challenges(user_id, status);
CREATE INDEX idx_security_challenges_policy ON security_challenges(workflow_mechanism_policy_id);
CREATE INDEX idx_security_challenges_status_expiry ON security_challenges(status, expires_at);
CREATE INDEX idx_authentication_attempts_user_time ON authentication_attempts(user_id, attempted_at);
CREATE INDEX idx_authentication_attempts_challenge_time ON authentication_attempts(security_challenge_id, attempted_at);
CREATE INDEX idx_authentication_attempts_identifier_ip_time
    ON authentication_attempts(attempted_identifier, ip_address, status, attempted_at);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens(family_id);
CREATE INDEX idx_refresh_tokens_session_id ON refresh_tokens(session_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

INSERT INTO roles (id, name) VALUES
    ('00000000-0000-0000-0000-000000000001', 'USER'),
    ('00000000-0000-0000-0000-000000000002', 'SUPPORT'),
    ('00000000-0000-0000-0000-000000000003', 'SUBADMIN'),
    ('00000000-0000-0000-0000-000000000004', 'ADMIN');

INSERT INTO authorities (id, name) VALUES
    ('00000000-0000-0000-0000-000000000101', 'USER_READ'),
    ('00000000-0000-0000-0000-000000000102', 'USER_UPDATE'),
    ('00000000-0000-0000-0000-000000000103', 'USER_DISABLE'),
    ('00000000-0000-0000-0000-000000000104', 'USER_ENABLE'),
    ('00000000-0000-0000-0000-000000000105', 'USER_BAN'),
    ('00000000-0000-0000-0000-000000000106', 'USER_UNBAN'),
    ('00000000-0000-0000-0000-000000000107', 'USER_DELETE'),
    ('00000000-0000-0000-0000-000000000108', 'ROLE_READ'),
    ('00000000-0000-0000-0000-000000000109', 'ROLE_ASSIGN'),
    ('00000000-0000-0000-0000-000000000110', 'ROLE_REVOKE'),
    ('00000000-0000-0000-0000-000000000111', 'AUDIT_LOG_READ'),
    ('00000000-0000-0000-0000-000000000112', 'PLATFORM_SETTINGS_READ'),
    ('00000000-0000-0000-0000-000000000113', 'PLATFORM_SETTINGS_UPDATE');

INSERT INTO workflow_types (id, code, name, description, created_at) VALUES
    ('00000000-0000-0000-0000-000000001001', 'PASSWORD_RECOVERY', 'Password recovery', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000001002', 'EMAIL_VERIFICATION', 'Email verification', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000001003', 'MFA', 'Multi-factor authentication', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000001004', 'EMAIL_CHANGE', 'Email change', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000001005', 'PHONE_VERIFICATION', 'Phone verification', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000001006', 'TRANSACTION_CONFIRMATION', 'Transaction confirmation', NULL, CURRENT_TIMESTAMP);

INSERT INTO challenge_mechanism_types (id, code, name, description, created_at) VALUES
    ('00000000-0000-0000-0000-000000002001', 'OTP', 'One-time password', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000002002', 'MAGIC_LINK', 'Magic link', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000002003', 'PASSKEY', 'Passkey', NULL, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000002004', 'AUTHENTICATOR_APP', 'Authenticator app', NULL, CURRENT_TIMESTAMP);

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.name = 'USER_READ'
WHERE role.name IN ('USER', 'SUPPORT', 'SUBADMIN', 'ADMIN');

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.name IN ('USER_UPDATE', 'ROLE_READ')
WHERE role.name IN ('SUPPORT', 'SUBADMIN', 'ADMIN');

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
JOIN authorities authority ON authority.name IN (
    'USER_DISABLE', 'USER_ENABLE', 'USER_BAN', 'USER_UNBAN', 'ROLE_ASSIGN', 'ROLE_REVOKE'
)
WHERE role.name IN ('SUBADMIN', 'ADMIN');

INSERT INTO role_authorities (role_id, authority_id)
SELECT role.id, authority.id
FROM roles role
CROSS JOIN authorities authority
WHERE role.name = 'ADMIN'
ON CONFLICT DO NOTHING;
