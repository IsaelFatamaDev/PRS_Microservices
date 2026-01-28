CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    organization_id VARCHAR(50) NOT NULL,
    zone_id VARCHAR(50),
    street_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT ck_users_document_type CHECK (document_type IN ('DNI', 'PASSPORT', 'RUC')),
    CONSTRAINT ck_users_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'CLIENT', 'OPERATOR')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_document_number ON users(document_number);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_zone_id ON users(zone_id);
CREATE INDEX idx_users_created_at ON users(created_at);
