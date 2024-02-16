# Migration steps

This guide details the steps to migrate the legacy portal Field consents system data (Oracle DB) into the new Spring Boot Field consents Postgres database.

This migration guide will make use of both the Energy Portal Oracle database and the FCS Postgres database.

The plan is to flatten the data required for migration and insert into tables in a new `fcs_migration` schema in the Oracle portal database ready for migration to the new FCS Postgres database.

## 1. Create the migration schema on the Energy Portal database

Run the following patch create the migration schema `fcs_migration`. Note, this will need to be run in a schema such as `XVIEWMGR` which has permission to create users and grant roles.
- `/energyportal/V01_create_migration_schema.sql`

## 2. Create the tables required for the migration 

On schema `fcs_migration` run the following patch:
- `/energyportal/V02_create_mirgation_data_tables.sql`

## 3. Stage/flatten the legacy field consents data ready for migration

On schema `fcs_migration` run the following patch:
- `/energyportal/V03_insert_mirgation_data.sql`

## 4. Stage/flatten the file data and migrate the files to S3

On schema `fcs_migration` run through the following patch:
- `/energyportal/V04_s3_file_migration.sql`

Part of the above will also involve running the S3 migration tool s3-file-migrator.
- https://github.com/Fivium/s3-file-migrator

### File migration method
1) insert data we have (inc blobs etc) into promotemgr.s3_file_migration
2) run the Java file migration tool (from the bastion for uat and prod)
3) insert data we have into fcs_migration.file_upload_library_uploaded_files (generate uuid here for the id)
4) push the data from Oracle to Postgres

## 5. Setup a DB link from Oracle to Postgres

Run through the following guide per environment (sys admin/DBA job):
- https://medium.com/analytics-vidhya/oracle-database-link-to-postgresql-database-b5ac1006f47a

Example final DB link creation from the Oracle side:
```
CREATE PUBLIC DATABASE LINK FCS_POSTGRES_DB
CONNECT TO "fcs_app"
IDENTIFIED BY <password>
USING 'fcs_postgres';
```

Environments - `local`, `dev`, `st`, `preprod`, `prod`

## 6a. Push the data over the DB link to the new field consents Postgres database

On schema `fcs_migration` run the following patch:
- `/energyportal/V05_push_mirgation_data.sql`

Note - you will need to have a clean DB to migrate to otherwise the ids will likely clash.

## 6b. Manual extract/import

CLOBs are not supported over the DB link so the following tables have been migrated manually via extract/import (for now):
- application_supporting_information
- application_case_notes
- application_updates
- application_technical_reviews

### Methdod
- Query the data in Toad
- Ctrl-A (select all the data) -> Right click "Export dataset..."
- Export as pipe separated txt file
- In IntelliJ connect to the appropriate Postgres DB and navigate to the appropriate table
- Right click -> Import/Export -> Import Data From File(s) -> select the appropriate file and choose the correct import setting for pipe separated data
- Run the Import

## 6. Post migration sync Postgres sequences

On the FCS Postgres database (`fcs` schema) run the following patch:
- `/energyportal/V06_restart_postgres_sequences.sql`

This will look at all the migrated ids and ensure the sequence next values are in sync.
