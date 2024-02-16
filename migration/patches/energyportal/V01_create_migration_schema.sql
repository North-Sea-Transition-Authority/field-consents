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

GRANT INSERT ON promotemgr.s3_file_migration TO fcs_migration;

GRANT DELETE ON promotemgr.s3_file_migration TO fcs_migration;