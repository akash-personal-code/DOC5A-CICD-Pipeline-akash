-- V2: Expand phase - add encrypted_email column
ALTER TABLE customers ADD COLUMN encrypted_email VARCHAR(255);

-- Record the schema change
INSERT INTO audit_events (event_type, details) 
VALUES ('SCHEMA_EXPANDED', 'Added encrypted_email column to customers table');
