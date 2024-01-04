
-- Full execution time
-- dev to local: 32mins 39secs

--SELECT *
--FROM "fcs"."applications"@fcs_postgres_db;
--/

--
-- wipe DB prior to migration run
--

---- audit tables (not migrating to but need clearing)
--DELETE FROM "fcs"."application_consultations_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."file_upload_library_uploaded_files_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_technical_reviews_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_updates_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_flags_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_versions_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."payments_library_payments_aud"@fcs_postgres_db;
--
---- we aren't migrating to these table but the data still needs deleting
--DELETE FROM "fcs"."application_work_area_priorities"@fcs_postgres_db;
--DELETE FROM "fcs"."application_rationale"@fcs_postgres_db;
--DELETE FROM "fcs"."application_withdrawals"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consultation_further_information"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consultations"@fcs_postgres_db;
--DELETE FROM "fcs"."payments_library_payments"@fcs_postgres_db;
--
---- delete data from tables we are migrating too
--DELETE FROM "fcs"."file_upload_library_uploaded_files"@fcs_postgres_db;
--DELETE FROM "fcs"."application_technical_reviews"@fcs_postgres_db;
--DELETE FROM "fcs"."application_updates"@fcs_postgres_db;
--DELETE FROM "fcs"."application_case_notes"@fcs_postgres_db;
--DELETE FROM "fcs"."vents"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_periods"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_gas_data"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_short_term_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_annual_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flares"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_report_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_report_periods"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_report_gas_data"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_short_term_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_annual_months"@fcs_postgres_db;
--DELETE FROM "fcs"."short_term_production_months"@fcs_postgres_db;
--DELETE FROM "fcs"."annual_production_months"@fcs_postgres_db;
--DELETE FROM "fcs"."long_term_production_years"@fcs_postgres_db;
--DELETE FROM "fcs"."application_supporting_information"@fcs_postgres_db;
--DELETE FROM "fcs"."application_eia_directions"@fcs_postgres_db;
--DELETE FROM "fcs"."application_flags"@fcs_postgres_db;
--DELETE FROM "fcs"."application_units"@fcs_postgres_db;
--DELETE FROM "fcs"."application_asset_licences"@fcs_postgres_db;
--DELETE FROM "fcs"."application_assets"@fcs_postgres_db;
--DELETE FROM "fcs"."consent_lengths"@fcs_postgres_db;
--DELETE FROM "fcs"."application_versions"@fcs_postgres_db;
--DELETE FROM "fcs"."applications"@fcs_postgres_db;

--
-- applications
--

-- Run times
-- dev to local: 38s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.applications) LOOP
  
    INSERT INTO "fcs"."applications"@fcs_postgres_db (
      "id"
    , "type"
    , "created_date"
    , "created_by_wua_id"
    , "variation_no"
    , "application_no"
    --, "fc_id"
    ) VALUES (
      rec.id
    , rec.type
    , rec.created_date
    , rec.created_by_wua_id
    , rec.variation_no
    , rec.application_no
    --, rec.fc_id -- TODO add flag?
    );
  
  END LOOP;

END;
/

--
-- application_versions
--

-- Run times
-- dev to local: 59s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_versions) LOOP
  
    INSERT INTO "fcs"."application_versions"@fcs_postgres_db (
      "id"
    , "application_id"
    , "version_no"
    , "primary_operator_ou_id"
    , "cached_primary_operator_name"
    , "status"
    , "created_date_time"
    , "created_by_wua_id"
    , "submitted_date_time"
    , "submitted_by_wua_id"
    , "case_officer_wua_id"
    , "cam_wua_id"
    ) VALUES (
      rec.id
    , rec.application_id
    , rec.version_no
    , rec.primary_operator_ou_id
    , rec.cached_primary_operator_name
    , rec.status
    , rec.created_date_time
    , rec.created_by_wua_id
    , rec.submitted_date_time
    , rec.submitted_by_wua_id
    , rec.case_officer_wua_id
    , rec.cam_wua_id
    );
  
  END LOOP;

END;
/


--
-- consent_lengths
--

-- Run times
-- dev to local: 58s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.consent_lengths) LOOP
  
    INSERT INTO "fcs"."consent_lengths"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "consent_length"
    , "annual_consent_year"
    , "short_term_start_date"
    , "short_term_end_date"
    , "long_term_start_year"
    , "long_term_end_year"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.consent_length
    , rec.annual_consent_year
    , rec.short_term_start_date
    , rec.short_term_end_date
    , rec.long_term_start_year
    , rec.long_term_end_year
    );
  
  END LOOP;

END;
/


--
-- application_assets
--

-- Run times
-- dev to local: 162s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_assets) LOOP
  
    INSERT INTO "fcs"."application_assets"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "asset_role"
    , "asset_no"
    , "asset_operator_ou_id"
    , "cached_asset_operator_name"
    , "asset_type"
    , "asset_id"
    , "cached_asset_name"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.asset_role
    , rec.asset_no
    , rec.asset_operator_ou_id
    , rec.cached_asset_operator_name
    , rec.asset_type
    , rec.asset_id
    , rec.cached_asset_name
    );
  
  END LOOP;

END;
/

--
-- application_asset_licences
--

-- Run time
-- dev to local: 167s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_asset_licences) LOOP
  
    INSERT INTO "fcs"."application_asset_licences"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "application_asset_id"
    , "licence_id"
    , "cached_licence_ref"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.application_asset_id
    , rec.licence_id
    , rec.cached_licence_ref
    );
  
  END LOOP;

END;
/

--
-- application_units
--

-- Run time
-- dev to local: 113s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_units) LOOP
  
    INSERT INTO "fcs"."application_units"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "flare_category_unit"
    , "vent_category_unit"
    , "production_oil_unit"
    , "production_gas_unit"
    , "flare_gas_density_unit"
    , "flare_gas_content_unit"
    , "vent_gas_density_unit"
    , "vent_gas_content_unit"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.flare_category_unit
    , rec.vent_category_unit
    , rec.production_oil_unit
    , rec.production_gas_unit
    , rec.flare_gas_density_unit
    , rec.flare_gas_content_unit
    , rec.vent_gas_density_unit
    , rec.vent_gas_content_unit
    );
  
  END LOOP;

END;
/

--
-- application_flags
--

-- Run time
-- dev to local: 181s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_flags) LOOP
  
    INSERT INTO "fcs"."application_flags"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "flag_type"
    , "flag_value"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.flag_type
    , rec.flag_value
    );
  
  END LOOP;

END;
/

--
-- application_eia_directions
--

-- Run time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_eia_directions) LOOP
  
    INSERT INTO "fcs"."application_eia_directions"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "for_purpose_of_eia_regs"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.for_purpose_of_eia_regs
    );
  
  END LOOP;

END;
/

--
-- application_supporting_information
--

--SELECT si.*, length(notes), length(erap_notes)
--FROM fcs_migration.application_supporting_information si 
--WHERE length(notes) > 4000 OR length(erap_notes) > 4000;
--/


-- TODO - deal with the CLOBs!
-- For now have migrated to a pipe separated txt file and used IntelliJ to import.
--SELECT *
--FROM fcs_migration.application_supporting_information
--ORDER BY id
--/
-- Run time
-- dev to local: s
--DECLARE
--  l_loop_count INTEGER;
--  l_notes VARCHAR2(32767);
--  l_erap_notes VARCHAR2(32767);
--BEGIN
--
--  FOR rec IN (
--    SELECT si.*
--    , length(si.notes) notes_length
--    , length(si.erap_notes) erap_notes_length
--    FROM fcs_migration.application_supporting_information si
--    WHERE length(si.notes) > 4000
--  ) LOOP
--  
----    l_loop_count := 1;
----  
----    l_notes := dbms_lob.substr(rec.notes, 4000, 1);
----    l_erap_notes := dbms_lob.substr(rec.erap_notes, 4000, 1);
----    
----    dbms_output.put_line('notes_length:'||rec.notes_length);
----    dbms_output.put_line('erap_notes_length:'||rec.erap_notes_length);
--  
--    INSERT INTO "fcs"."application_supporting_information"@fcs_postgres_db (
--      "id"
--    , "application_version_id"
--    , "notes"
--    , "erap_notes"
--    ) VALUES (
--      rec.id
--    , rec.application_version_id
--    , rec.notes_varchar2
--    , 'test erap'
--    );
--  
----    IF (rec.notes_length > 4000) THEN
----    
----      l_loop_count := ceil(notes_length/4000) - 1;
----    
----      FOR i IN 1..l_loop_count LOOP
----      
----        UPDATE "fcs"."application_supporting_information"@fcs_postgres_db
----        SET "notes" = "notes"||substr(rec.notes, l_loop_count*4000+1, l_loop_count*4000+4000)
----        WHERE "id" = rec.id;
----      
----      END LOOP;
----    
----    END IF;
--  
--  END LOOP;
--
--END;
--/

--
-- long_term_production_years
--

-- Execution time
-- dev to local: 80s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.long_term_production_years) LOOP
  
    INSERT INTO "fcs"."long_term_production_years"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "oil_min_value"
    , "oil_max_value"
    , "gas_min_value"
    , "gas_max_value"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.oil_min_value
    , rec.oil_max_value
    , rec.gas_min_value
    , rec.gas_max_value
    );
  
  END LOOP;

END;
/

--
-- annual_production_months
--

-- Execution time
-- dev to local: 86s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.annual_production_months) LOOP
  
    INSERT INTO "fcs"."annual_production_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "oil_min_value"
    , "oil_max_value"
    , "gas_min_value"
    , "gas_max_value"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.oil_min_value
    , rec.oil_max_value
    , rec.gas_min_value
    , rec.gas_max_value
    );
  
  END LOOP;

END;
/

--
-- short_term_production_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.short_term_production_months) LOOP
  
    INSERT INTO "fcs"."short_term_production_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "start_date"
    , "end_date"
    , "oil_min_value"
    , "oil_max_value"
    , "gas_min_value"
    , "gas_max_value"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.start_date
    , rec.end_date
    , rec.oil_min_value
    , rec.oil_max_value
    , rec.gas_min_value
    , rec.gas_max_value
    );
  
  END LOOP;

END;
/

--
-- flare_annual_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_annual_months) LOOP
  
    INSERT INTO "fcs"."flare_annual_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_a"
    , "category_b"
    , "category_c"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- flare_short_term_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_short_term_months) LOOP
  
    INSERT INTO "fcs"."flare_short_term_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "start_date"
    , "end_date"
    , "category_a"
    , "category_b"
    , "category_c"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.start_date
    , rec.end_date
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- flare_report_gas_data
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_gas_data) LOOP
  
    INSERT INTO "fcs"."flare_report_gas_data"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "category_a_density"
    , "category_a_inert_percentage"
    , "category_a_hydro_percentage"
    , "category_b_density"
    , "category_b_inert_percentage"
    , "category_b_hydro_percentage"
    , "category_c_density"
    , "category_c_inert_percentage"
    , "category_c_hydro_percentage"
    , "evaluated_per_category"
    , "evaluated_per_category_explanation"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.category_a_density
    , rec.category_a_inert_percentage
    , rec.category_a_hydro_percentage
    , rec.category_b_density
    , rec.category_b_inert_percentage
    , rec.category_b_hydro_percentage
    , rec.category_c_density
    , rec.category_c_inert_percentage
    , rec.category_c_hydro_percentage
    , rec.evaluated_per_category
    , rec.evaluated_per_category_explanation
    );
  
  END LOOP;

END;
/

--
-- flare_report_periods
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_periods) LOOP
  
    INSERT INTO "fcs"."flare_report_periods"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "report_end_month"
    , "report_end_year"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.report_end_month
    , rec.report_end_year
    );
  
  END LOOP;

END;
/

--
-- flare_report_months
--

-- Execution time
-- dev to local: 2s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_months) LOOP
  
    INSERT INTO "fcs"."flare_report_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_a"
    , "category_b"
    , "category_c"
    , "shut_down_days"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.shut_down_days
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- flares
--

-- Execution time
-- dev to local: 45s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flares) LOOP
  
    INSERT INTO "fcs"."flares"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "flare_no"
    , "flare_type"
    , "description"
    , "metered_flag"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.flare_no
    , rec.flare_type
    , rec.description
    , rec.metered_flag
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vent_annual_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_annual_months) LOOP
  
    INSERT INTO "fcs"."vent_annual_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_a"
    , "category_b"
    , "category_c"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vent_short_term_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_short_term_months) LOOP
  
    INSERT INTO "fcs"."vent_short_term_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "start_date"
    , "end_date"
    , "category_a"
    , "category_b"
    , "category_c"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.start_date
    , rec.end_date
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vent_report_gas_data
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_gas_data) LOOP
  
    INSERT INTO "fcs"."vent_report_gas_data"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "category_a_density"
    , "category_a_inert_percentage"
    , "category_a_hydro_percentage"
    , "category_b_density"
    , "category_b_inert_percentage"
    , "category_b_hydro_percentage"
    , "category_c_density"
    , "category_c_inert_percentage"
    , "category_c_hydro_percentage"
    , "evaluated_per_category"
    , "evaluated_per_category_explanation"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.category_a_density
    , rec.category_a_inert_percentage
    , rec.category_a_hydro_percentage
    , rec.category_b_density
    , rec.category_b_inert_percentage
    , rec.category_b_hydro_percentage
    , rec.category_c_density
    , rec.category_c_inert_percentage
    , rec.category_c_hydro_percentage
    , rec.evaluated_per_category
    , rec.evaluated_per_category_explanation
    );
  
  END LOOP;

END;
/

--
-- vent_report_periods
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_periods) LOOP
  
    INSERT INTO "fcs"."vent_report_periods"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "report_end_month"
    , "report_end_year"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.report_end_month
    , rec.report_end_year
    );
  
  END LOOP;

END;
/

--
-- vent_report_months
--

-- Execution time
-- dev to local: 1s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_months) LOOP
  
    INSERT INTO "fcs"."vent_report_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_a"
    , "category_b"
    , "category_c"
    , "shut_down_days"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_a
    , rec.category_b
    , rec.category_c
    , rec.shut_down_days
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vents
--

-- Execution time
-- dev to local: 13s
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vents) LOOP
  
    INSERT INTO "fcs"."vents"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "vent_no"
    , "vent_type"
    , "description"
    , "metered_flag"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.vent_no
    , rec.vent_type
    , rec.description
    , rec.metered_flag
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- case_notes
--

-- TODO - getting error
--ORA-65510: Distributed LOB operations are not supported on pre-12.2 databases.
--ORA-06512: at line 5
--ORA-06512: at line 5

-- For now have migrated to a pipe separated txt file and used IntelliJ to import.
--SELECT
--  id
--, application_version_id
--, added_by_wua_id
--, to_char(added_date_time, 'YYYY-MM-DD HH24:MI:SS') added_date_time
--, case_note_text
--FROM fcs_migration.application_case_notes
--ORDER BY id
--/

-- Execution time
-- dev to local: s
--BEGIN
--
--  FOR rec IN (SELECT * FROM fcs_migration.application_case_notes) LOOP
--  
--    INSERT INTO "fcs"."application_case_notes"@fcs_postgres_db (
--      "id"
--    , "application_version_id"
--    , "added_by_wua_id"
--    , "added_date_time"
--    , "case_note_text"
--    ) VALUES (
--      rec.id
--    , rec.application_version_id
--    , rec.added_by_wua_id
--    , rec.added_date_time
--    , rec.case_note_text
--    );
--  
--  END LOOP;
--
--END;
--/

--
-- application_updates
--

-- TODO - getting error
--ORA-65510: Distributed LOB operations are not supported on pre-12.2 databases.
--ORA-06512: at line 5
--ORA-06512: at line 5

-- For now have migrated to a pipe separated txt file and used IntelliJ to import.
--SELECT
--  id
--, application_version_id
--, requested_by_wua_id
--, to_char(requested_date_time, 'YYYY-MM-DD HH24:MI:SS') requested_date_time
--, request_text
--, to_char(deadline_date_time, 'YYYY-MM-DD HH24:MI:SS') deadline_date_time
--, responded_by_wua_id
--, to_char(responded_date_time, 'YYYY-MM-DD HH24:MI:SS') responded_date_time
--, response_text
--, response_type
--, application_update_status
--, response_application_version_id
--FROM fcs_migration.application_updates
--ORDER BY id
--/

-- Execution time
-- dev to local: s
--BEGIN
--
--  FOR rec IN (SELECT * FROM fcs_migration.application_updates) LOOP
--  
--    INSERT INTO "fcs"."application_updates"@fcs_postgres_db (
--      "id"
--    , "application_version_id"
--    , "requested_by_wua_id"
--    , "requested_date_time"
--    , "request_text"
--    , "deadline_date_time"
--    , "responded_by_wua_id"
--    , "responded_date_time"
--    , "response_text"
--    , "response_type"
--    , "application_update_status"
--    , "response_application_version_id"
--    ) VALUES (
--      rec.id
--    , rec.application_version_id
--    , rec.requested_by_wua_id
--    , rec.requested_date_time
--    , rec.request_text
--    , rec.deadline_date_time
--    , rec.responded_by_wua_id
--    , rec.responded_date_time
--    , rec.response_text
--    , rec.response_type
--    , rec.application_update_status
--    , rec.response_application_version_id
--    );
--  
--  END LOOP;
--
--END;
--/

--
-- application_technical_reviews
--

-- TODO - getting error
--ORA-65510: Distributed LOB operations are not supported on pre-12.2 databases.
--ORA-06512: at line 5
--ORA-06512: at line 5

---- For now have migrated to a pipe separated txt file and used IntelliJ to import.
--SELECT
--  id
--, request_application_version_id
--, requested_by_wua_id
--, to_char(requested_date_time, 'YYYY-MM-DD HH24:MI:SS') requested_date_time
--, request_text
--, to_char(deadline_date_time, 'YYYY-MM-DD HH24:MI:SS') deadline_date_time
--, technical_reviewer_wua_id
--, responded_by_wua_id
--, to_char(responded_date_time, 'YYYY-MM-DD HH24:MI:SS') responded_date_time
--, response_text
--, response_type
--, technical_review_status
--, response_application_version_id
--FROM fcs_migration.application_technical_reviews
--ORDER BY id
--/

-- Execution time
-- dev to local: s
--BEGIN
--
--  FOR rec IN (SELECT * FROM fcs_migration.application_technical_reviews) LOOP
--  
--    INSERT INTO "fcs"."application_technical_reviews"@fcs_postgres_db (
--      "id"
--    , "request_application_version_id"
--    , "requested_by_wua_id"
--    , "requested_date_time"
--    , "request_text"
--    , "deadline_date_time"
--    , "technical_reviewer_wua_id"
--    , "responded_by_wua_id"
--    , "responded_date_time"
--    , "response_text"
--    , "response_type"
--    , "technical_review_status"
--    , "response_application_version_id"
--    ) VALUES (
--      rec.id
--    , rec.request_application_version_id
--    , rec.requested_by_wua_id
--    , rec.requested_date_time
--    , rec.request_text
--    , rec.deadline_date_time
--    , rec.technical_reviewer_wua_id
--    , rec.responded_by_wua_id
--    , rec.responded_date_time
--    , rec.response_text
--    , rec.response_type
--    , rec.technical_review_status
--    , rec.response_application_version_id
--    );
--  
--  END LOOP;
--
--END;
--/

--
-- file_upload_library_uploaded_files
--
-- TODO

--BEGIN
--
--  FOR rec IN (SELECT * FROM fcs_migration.) LOOP
--  
--    INSERT INTO "fcs".""@fcs_postgres_db (
--    ) VALUES (
--    );
--  
--  END LOOP;
--
--END;
--/
