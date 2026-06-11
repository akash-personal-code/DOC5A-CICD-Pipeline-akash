-- V4: Contract phase - drop the old email column
-- DO NOT RUN AUTOMATICALLY
-- This should only be run after all application instances have been updated
-- to use the encrypted_email column and no longer rely on the plain email column.

/*
ALTER TABLE customers DROP COLUMN email;

INSERT INTO audit_events (event_type, details) 
VALUES ('SCHEMA_CONTRACTED', 'Dropped email column from customers table');
*/
