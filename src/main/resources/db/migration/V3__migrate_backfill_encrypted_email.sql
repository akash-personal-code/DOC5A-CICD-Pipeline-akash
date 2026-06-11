-- V3: Migrate phase - backfill encrypted_email from email
-- In a real scenario, this might involve an application script or a stored proc 
-- that actually encrypts the data. For this demo, we'll just prepend 'ENCRYPTED_'
UPDATE customers SET encrypted_email = CONCAT('ENCRYPTED_', email) WHERE email IS NOT NULL AND encrypted_email IS NULL;

-- Record the schema change
INSERT INTO audit_events (event_type, details) 
VALUES ('SCHEMA_MIGRATED', 'Backfilled encrypted_email column in customers table');
