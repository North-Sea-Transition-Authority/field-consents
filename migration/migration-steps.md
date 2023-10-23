# Migration steps

This guide details the steps to migrate the legacy portal Field consents system data (Oracle DB) into the new Spring Boot Field consents Postgres database.

This migration guide will make use of both the Energy Portal Oracle database and the FCS Postgres database.

The plan is to flatten the data required for migration and insert into tables in a new fcs_migration schema in the Oracle portal database ready for migration to the new FCS Postgres database.

It is still undecided how we will get the data from one DB into the other:
1. Using a database link
2. Extracting the data (using Toad) and manually importing (using PGAdmin) 

## 1. Create the migration schema on the Energy Portal database

Run the following patches to construct the required tables for the migration. Note, this will need to be in a schema such as `XVIEWMGR` which has permission to make tables in other schemas.

- `/energyportal/V01_create_migration_schema.sql`
- `/energyportal/V02_create_mirgation_data_tables.sql`

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
