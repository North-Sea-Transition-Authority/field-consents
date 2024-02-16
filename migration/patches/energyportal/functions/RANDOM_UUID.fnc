CREATE OR REPLACE FUNCTION fcs_migration.random_uuid RETURN VARCHAR2 AS
LANGUAGE JAVA
NAME 'java.util.UUID.randomUUID() return String';
