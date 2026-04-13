-- V1: Instituciones educativas
-- Fuente: AD-08 (multi-tenancy), AD-01

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE institutions (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(200) NOT NULL,
    email_domain VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_institutions_email_domain UNIQUE (email_domain)
);

COMMENT ON TABLE institutions IS 'Instituciones educativas registradas en la plataforma. Solo SUPERADMIN puede crear registros.';
