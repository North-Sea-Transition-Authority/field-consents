CREATE USER fcs_migration IDENTIFIED BY "<password>"
DEFAULT TABLESPACE tbsdata
TEMPORARY TABLESPACE TEMP
PROFILE mgr_user
QUOTA UNLIMITED ON TBSBLOB
QUOTA UNLIMITED ON TBSCLOB
QUOTA UNLIMITED ON TBSDATA
QUOTA UNLIMITED ON TBSIDX;

GRANT CREATE SESSION TO fcs_migration;

GRANT SELECT ANY TABLE TO fcs_migration;

GRANT CREATE TABLE TO fcs_migration;

GRANT CREATE SEQUENCE TO fcs_migration;

GRANT CREATE VIEW TO fcs_migration;

GRANT CREATE PROCEDURE TO fcs_migration;

GRANT EXECUTE ON securemgr.secure_lob TO fcs_migration;

-- S3 File Migration (run on promotemgr if needed)
--RENAME s3_file_migration TO s3_file_migration_pon15;
--
--CREATE TABLE promotemgr.s3_file_migration(
--  fox_file_id VARCHAR2(4000) NOT NULL
--, application VARCHAR2(4000) NOT NULL
--, reference VARCHAR2(4000)
--, directory VARCHAR2(4000) NOT NULL
--, filename VARCHAR2(4000) NOT NULL
--, content BLOB NOT NULL
--, s3_endpoint VARCHAR2(4000)
--, s3_bucket VARCHAR2(4000)
--, s3_path VARCHAR2(4000)
--, migrated_timestamp TIMESTAMP
--);

GRANT INSERT ON promotemgr.s3_file_migration TO fcs_migration;

GRANT UPDATE ON promotemgr.s3_file_migration TO fcs_migration;

GRANT DELETE ON promotemgr.s3_file_migration TO fcs_migration;
