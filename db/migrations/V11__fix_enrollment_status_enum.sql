-- V11: Fix enrollment_status enum type conflict with Hibernate EnumType.STRING
-- Reason: Hibernate with @Enumerated(EnumType.STRING) tries to insert VARCHAR values,
-- but PostgreSQL ENUM type rejects non-enum values. Solution: use VARCHAR instead.

-- Drop the check constraint that references the enum type
ALTER TABLE enrollments DROP CONSTRAINT ck_enrollments_cancelled_at;

-- Convert the status column from enrollment_status ENUM to VARCHAR
ALTER TABLE enrollments ALTER COLUMN status TYPE VARCHAR(50);

-- Recreate the check constraint
ALTER TABLE enrollments ADD CONSTRAINT ck_enrollments_cancelled_at
    CHECK (status = 'ACTIVE' OR cancelled_at IS NOT NULL);

-- Drop the unused enum type
DROP TYPE enrollment_status;
