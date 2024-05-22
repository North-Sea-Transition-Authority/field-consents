
--DELETE FROM promotemgr.s3_file_migration WHERE application = 'FCS';
--DELETE FROM fcs_migration.file_upload_library_uploaded_files;

--
-- file_upload_library_uploaded_files / promotemgr.s3_file_migration
--
-- supporting info docs
--


-- Execution run time for insert into promotemgr.s3_file_migration :
-- UAT 4 mins 37 secs (for 5171 files)
-- queue the files to be migrated
INSERT INTO promotemgr.s3_file_migration (
  fox_file_id
, application
, reference
, directory
, filename
, content
)
SELECT
  sd.fox_file_id
, 'FCS' application
, av.id reference
, 'migrated' directory
, sd.fox_file_id filename
, sd.file_blob_content content
FROM fcs_migration.application_versions av
JOIN fcs_migration.field_consent_supporting_docs sd ON sd.fcd_id = av.id
WHERE sd.calculated_file_size > 0;
/
COMMIT;
/
-- Execution run time for insert into promotemgr.s3_file_migration :
-- UAT 2 mins 55 secs (for 21257 files)
INSERT INTO promotemgr.s3_file_migration ( 
  fox_file_id
, application
, reference
, directory
, filename
, content
)
SELECT
  cd.dummy_fox_file_id
, 'FCS' application
, cd.fci_id reference
, 'migrated' directory
, cd.dummy_fox_file_id filename -- this isn't displayed to the user but has to be unique in the S3 bucket
, cd.file_blob_content content
FROM fcs_migration.application_versions av -- this ensures that any consent docs have a migrated app version (they all do but this is a belt and braces)
JOIN fcs_migration.field_consent_consent_docs cd ON cd.fcd_id = av.id
WHERE cd.calculated_file_size > 0;
/
COMMIT;
/

-- *********************************************
-- Now run the migration tool (s3_file_migrator)
-- *********************************************
--
-- Examples (local and dev) (can also use the VERIFY mode to check that he upload to S3 has worked)
-- java -DdbUrl=db-ogadev1.sb2.dev:1521/ogadev1 -DdbUser=promotemgr -DdbPassword=? -DaccessKey=dummy -DsecretKey=dummy -Dbucket=field-consents -Dregion=example -DendpointUrl=http://localhost:9090 -jar build/libs/s3-file-migrator.jar MIGRATE
-- java -DdbUrl=db-ogadev1.sb2.dev:1521/ogadev1 -DdbUser=promotemgr -DdbPassword=? -DaccessKey=? -DsecretKey=? -Dbucket=fcs.dev.fivium.co.uk -Dregion=eu-west-2 -DendpointUrl=s3.eu-west-2.amazonaws.com -jar build/libs/s3-file-migrator.jar MIGRATE
-- java -DdbUrl=db-ogast1.sb2.dev:1521/ogast1 -DdbUser=promotemgr -DdbPassword=? -DaccessKey=? -DsecretKey=? -Dbucket=fcs.st.fivium.co.uk -Dregion=eu-west-2 -DendpointUrl=s3.eu-west-2.amazonaws.com -jar build/libs/s3-file-migrator.jar MIGRATE
-- Run from Powershell on the Bastion (needs the double quotes) 
-- java -DdbUrl="db-ogacl1.oga.sb1.prod:1521/ogacl1" -DdbUser="promotemgr" -DdbPassword="?" -DaccessKey="?" -DsecretKey="?" -Dbucket="fcs.preprod.nstauthority.co.uk" -Dregion="eu-west-2" -DendpointUrl="s3.eu-west-2.amazonaws.com" -jar ./s3-file-migrator.jar MIGRATE
--
-- Execution run times for uploading the files to S3 using the s3_file_migrator
-- UAT 11 mins 15 secs (for 26428 files - 10.03GB)

-- queue the FUSS data for each migrated file
INSERT INTO fcs_migration.file_upload_library_uploaded_files (
  id
, bucket
, key
, name
, content_type
, content_length
, uploaded_at
, usage_id
, usage_type
, document_type
, description
, uploaded_by
)
SELECT
  fcs_migration.random_uuid()
, fm.s3_bucket bucket
, fm.s3_path key
, sd.filename name
, sd.content_type
, sd.calculated_file_size content_length
, sd.upload_date_time uploaded_at
, av.id usage_id
, 'ApplicationVersion' usage_type
, 'supporting-document' document_type
, sd.file_description description
, sd.uploaded_by_wua_id uploaded_by
FROM fcs_migration.application_versions av
JOIN fcs_migration.field_consent_supporting_docs sd ON sd.fcd_id = av.id
JOIN promotemgr.s3_file_migration fm ON fm.fox_file_id = sd.fox_file_id
WHERE fm.migrated_timestamp IS NOT NULL;
/
COMMIT;
/
INSERT INTO fcs_migration.file_upload_library_uploaded_files (
  id
, bucket
, key
, name
, content_type
, content_length
, uploaded_at
, usage_id
, usage_type
, document_type
, description
, uploaded_by
)
SELECT
  fcs_migration.random_uuid()
, fm.s3_bucket bucket
, fm.s3_path key
, cd.filename name
, cd.content_type
, cd.calculated_file_size content_length
, cd.upload_date_time uploaded_at
, cd.fci_id usage_id
, 'ApplicationConsent' usage_type
, 'generated-consent-document' document_type
, cd.file_description description
, cd.uploaded_by_wua_id uploaded_by
FROM fcs_migration.application_versions av
JOIN fcs_migration.field_consent_consent_docs cd ON cd.fcd_id = av.id
JOIN promotemgr.s3_file_migration fm ON fm.fox_file_id = cd.dummy_fox_file_id
WHERE fm.migrated_timestamp IS NOT NULL
/
COMMIT;
/
