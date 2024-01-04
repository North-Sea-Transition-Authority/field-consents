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

## 4. Setup a DB link from Oracle to Postgres

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

## 5a. Push the data over the DB link to the new field consents Postgres database

On schema `fcs_migration` run the following patch:
- `/energyportal/V04_push_mirgation_data.sql`

Note - you will need to have a clean DB to migrate to otherwise the ids will likely clash.

## 5b. Manual extract/import

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

# Scratch notes

FLARE ANNUAL MIGRATION (Legacy test case FCON/2041/0 (Version 1), local test case FCON/28/0 (Version 1))
- on the legacy system the report page subtracts the shutdown days prior to calculating the daily avergage...this seems wrong
- Data we don't have in new FCS 
  - Cover info - Year 2023 History - Flare Consent (tonnes/day)
  - Flare report
    - Stream Mol Wt is now -> Standard density (kg/m3)
    - Inert Gas Content (mol %) (or specify full composition) is now -> Inert gas content (mass %)
    - Hydrocarbon content (mol %) (or specify full composition) is now -> Hydrocarbon content (mass %)
  - Additional info - Does the activity as described in the consent application constitute a project under the Offshore Oil and
  Gas Exploration, Production, Unloading and Storage (Environmental Impact Assessment) Regulations 2020? Yes/No
  - Application copy PDF

CASE NOTES
- where are these in the legacy system? (intentions?)

PAYMENTS
- what do I need to migrate for this?
