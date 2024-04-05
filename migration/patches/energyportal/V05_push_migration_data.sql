
-- Full execution time
-- dev to local: 32mins 39secs - run 1
-- dev to local: 12mins 01secs - run 2
-- dev to dev: 8mins 03secs
-- st to st: 3mins 5secs - run 1
-- uat to uat: 13min 42secs - full run 1

--SELECT count(*)
--FROM "fcs"."applications"@fcs_postgres_db;
--/


--
-- wipe DB prior to migration run
--

---- audit tables (not migrating to but need clearing)
--DELETE FROM "fcs"."application_consent_issuing_approvals_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consultations_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."file_upload_library_uploaded_files_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_technical_reviews_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_updates_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_flags_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."application_versions_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."payments_library_payments_aud"@fcs_postgres_db;
--DELETE FROM "fcs"."notification_library_notifications_aud"@fcs_postgres_db;
--
---- we aren't migrating to these table but the data still needs deleting
--DELETE FROM "fcs"."application_consent_data_long_term_production_figures"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consent_data"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consent_issuing_approvals"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consents"@fcs_postgres_db;
--DELETE FROM "fcs"."application_work_area_priorities"@fcs_postgres_db;
--DELETE FROM "fcs"."application_rationale"@fcs_postgres_db;
--DELETE FROM "fcs"."application_withdrawals"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consultation_further_information"@fcs_postgres_db;
--DELETE FROM "fcs"."application_consultations"@fcs_postgres_db;
--DELETE FROM "fcs"."payments_library_payments"@fcs_postgres_db;
--DELETE FROM "fcs"."notification_library_notifications"@fcs_postgres_db;
--
---- delete data from tables we are migrating too
--DELETE FROM "fcs"."split_clob_legacy_data"@fcs_postgres_db;
--DELETE FROM "fcs"."application_other_legacy_data"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_long_term_years"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_long_term_years"@fcs_postgres_db;
--DELETE FROM "fcs"."file_upload_library_uploaded_files"@fcs_postgres_db;
--DELETE FROM "fcs"."application_technical_reviews"@fcs_postgres_db;
--DELETE FROM "fcs"."application_updates"@fcs_postgres_db;
--DELETE FROM "fcs"."application_case_notes"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_123_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_123_gas_data"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_short_term_123_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_annual_123_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vents"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_periods"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_report_gas_data"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_short_term_months"@fcs_postgres_db;
--DELETE FROM "fcs"."vent_annual_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_report_123_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_report_123_gas_data"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_short_term_123_months"@fcs_postgres_db;
--DELETE FROM "fcs"."flare_annual_123_months"@fcs_postgres_db;
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

---- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
--BEGIN
--  COMMIT;
--  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
--END;
--/

--
-- applications
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.applications ORDER BY id) LOOP
  
    INSERT INTO "fcs"."applications"@fcs_postgres_db (
      "id"
    , "type"
    , "created_date"
    , "created_by_wua_id"
    , "variation_no"
    , "application_no"
    ) VALUES (
      rec.id
    , rec.type
    , rec.created_date
    , rec.created_by_wua_id
    , rec.variation_no
    , rec.application_no
    );
  
  END LOOP;

END;
/

--
-- application_versions
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_versions WHERE id > 0 ORDER BY id) LOOP
  
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
    , "current_case_owner"
    , "migrated"
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
    , rec.current_case_owner
    , rec.migrated
    );
 
  END LOOP;

END;
/


--
-- consent_lengths
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.consent_lengths WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_assets WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_asset_licences WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_units WHERE id > 0 ORDER BY id) LOOP
  
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
    , "emission_category_type"
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
    , rec.emission_category_type
    );
  
  END LOOP;

END;
/

--
-- application_flags
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_flags WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_eia_directions WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (
    SELECT si.*
    , CASE
      WHEN length(si.notes) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(si.notes)
      END notes_varchar2
    , CASE
      WHEN length(si.erap_notes) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(si.erap_notes)
      END erap_notes_varchar2
    FROM fcs_migration.application_supporting_information si
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
    INSERT INTO "fcs"."application_supporting_information"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "notes"
    , "erap_notes"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.notes_varchar2
    , rec.erap_notes_varchar2
    );
  
  END LOOP;

END;
/


-- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/

--
-- long_term_production_years
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.long_term_production_years WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.annual_production_months WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.short_term_production_months WHERE id > 0 ORDER BY id) LOOP
  
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

-- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/

--
-- flare_annual_months
--
BEGIN

  FOR rec IN (
    SELECT t.*
    , CASE
      WHEN length(t.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(t.comments)
      END comments_varchar2
    FROM fcs_migration.flare_annual_months t
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
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
    , rec.comments_varchar2
    );
  
  END LOOP;

END;
/

--
-- flare_annual_123_months
--
BEGIN

  FOR rec IN (
    SELECT t.*
    , CASE
      WHEN length(t.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(t.comments)
      END comments_varchar2
    FROM fcs_migration.flare_annual_123_months t
    WHERE id > 0
    ORDER BY id
  ) LOOP

    INSERT INTO "fcs"."flare_annual_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_1"
    , "category_2"
    , "category_3"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_1
    , rec.category_2
    , rec.category_3
    , rec.comments_varchar2
    );

  END LOOP;

END;
/

--
-- flare_short_term_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_short_term_months WHERE id > 0 ORDER BY id) LOOP
  
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
-- flare_short_term_123_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_short_term_123_months WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."flare_short_term_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "start_date"
    , "end_date"
    , "category_1"
    , "category_2"
    , "category_3"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.start_date
    , rec.end_date
    , rec.category_1
    , rec.category_2
    , rec.category_3
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- flare_long_term_years
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_long_term_years WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."flare_long_term_years"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "gas"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.gas
    );
  
  END LOOP;

END;
/

--
-- flare_report_gas_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_gas_data WHERE id > 0 ORDER BY id) LOOP
  
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
-- flare_report_123_gas_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_123_gas_data WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."flare_report_123_gas_data"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "category_1_density"
    , "category_1_inert_percentage"
    , "category_1_hydro_percentage"
    , "category_2_density"
    , "category_2_inert_percentage"
    , "category_2_hydro_percentage"
    , "category_3_density"
    , "category_3_inert_percentage"
    , "category_3_hydro_percentage"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.category_1_density
    , rec.category_1_inert_percentage
    , rec.category_1_hydro_percentage
    , rec.category_2_density
    , rec.category_2_inert_percentage
    , rec.category_2_hydro_percentage
    , rec.category_3_density
    , rec.category_3_inert_percentage
    , rec.category_3_hydro_percentage
    );
  
  END LOOP;

END;
/


--
-- flare_report_periods
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_periods WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (
    SELECT rm.*
    , CASE
      WHEN length(rm.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(rm.comments)
      END comments_varchar2
    FROM fcs_migration.flare_report_months rm
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
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
    , rec.comments_varchar2
    );
  
  END LOOP;

END;
/

--
-- flare_report_123_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flare_report_123_months WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."flare_report_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_1"
    , "category_2"
    , "category_3"
    , "shut_down_days"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_1
    , rec.category_2
    , rec.category_3
    , rec.shut_down_days
    , rec.comments
    );
  
  END LOOP;

END;
/


--
-- flares
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.flares WHERE id > 0 ORDER BY id) LOOP
  
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

-- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/

--
-- vent_annual_months
--
BEGIN

  FOR rec IN (
    SELECT t.*
    , CASE
      WHEN length(t.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(t.comments)
      END comments_varchar2
    FROM fcs_migration.vent_annual_months t
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
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
    , rec.comments_varchar2
    );
  
  END LOOP;

END;
/

--
-- vent_annual_123_months
--
BEGIN

  FOR rec IN (
    SELECT t.*
    , CASE
      WHEN length(t.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(t.comments)
      END comments_varchar2
    FROM fcs_migration.vent_annual_123_months t
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
    INSERT INTO "fcs"."vent_annual_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_1"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_1
    , rec.comments_varchar2
    );
  
  END LOOP;

END;
/


--
-- vent_short_term_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_short_term_months WHERE id > 0 ORDER BY id) LOOP
  
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
-- vent_short_term_123_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_short_term_123_months WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."vent_short_term_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "start_date"
    , "end_date"
    , "category_1"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.start_date
    , rec.end_date
    , rec.category_1
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vent_long_term_years
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_long_term_years WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."vent_long_term_years"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "gas"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.gas
    );
  
  END LOOP;

END;
/

--
-- vent_report_gas_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_gas_data WHERE id > 0 ORDER BY id) LOOP
  
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
-- vent_report_123_gas_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_123_gas_data WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."vent_report_123_gas_data"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "category_1_density"
    , "category_1_inert_percentage"
    , "category_1_hydro_percentage"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.category_1_density
    , rec.category_1_inert_percentage
    , rec.category_1_hydro_percentage
    );
  
  END LOOP;

END;
/

--
-- vent_report_periods
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_periods WHERE id > 0 ORDER BY id) LOOP
  
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
BEGIN

  FOR rec IN (
    SELECT rm.*
    , CASE
      WHEN length(rm.comments) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(rm.comments)
      END comments_varchar2
    FROM fcs_migration.vent_report_months rm
    WHERE id > 0
    ORDER BY id  
  ) LOOP
  
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
    , rec.comments_varchar2
    );
  
  END LOOP;

END;
/

--
-- vent_report_123_months
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vent_report_123_months WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."vent_report_123_months"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "year"
    , "month"
    , "category_1"
    , "shut_down_days"
    , "comments"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.year
    , rec.month
    , rec.category_1
    , rec.shut_down_days
    , rec.comments
    );
  
  END LOOP;

END;
/

--
-- vents
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.vents WHERE id > 0 ORDER BY id) LOOP
  
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

-- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/

--
-- application_case_notes
--
BEGIN

  FOR rec IN (
    SELECT cn.*
    , CASE
      WHEN length(cn.case_note_text) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(cn.case_note_text)
      END case_note_text_varchar2
    FROM fcs_migration.application_case_notes cn
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
    INSERT INTO "fcs"."application_case_notes"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "added_by_wua_id"
    , "added_date_time"
    , "case_note_text"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.added_by_wua_id
    , rec.added_date_time
    , rec.case_note_text_varchar2
    );
  
  END LOOP;

END;
/

--
-- application_updates
--
BEGIN

  FOR rec IN (
    SELECT au.*
    , CASE
      WHEN length(au.request_text) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(au.request_text)
      END request_text_varchar2
    FROM fcs_migration.application_updates au
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
    INSERT INTO "fcs"."application_updates"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "requested_by_wua_id"
    , "requested_date_time"
    , "request_text"
    , "deadline_date_time"
    , "responded_by_wua_id"
    , "responded_date_time"
    , "response_text"
    , "response_type"
    , "application_update_status"
    , "response_application_version_id"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.requested_by_wua_id
    , rec.requested_date_time
    , rec.request_text_varchar2
    , rec.deadline_date_time
    , rec.responded_by_wua_id
    , rec.responded_date_time
    , rec.response_text
    , rec.response_type
    , rec.application_update_status
    , rec.response_application_version_id
    );
  
  END LOOP;

END;
/


--
-- application_technical_reviews
--
BEGIN

  FOR rec IN (
    SELECT tr.*
    , CASE
      WHEN length(tr.response_text) > 4000 THEN 'dummy text placeholder'
      ELSE to_char(tr.response_text)
      END response_text_varchar2
    FROM fcs_migration.application_technical_reviews tr
    WHERE id > 0
    ORDER BY id
  ) LOOP
  
    INSERT INTO "fcs"."application_technical_reviews"@fcs_postgres_db (
      "id"
    , "request_application_version_id"
    , "requested_by_wua_id"
    , "requested_date_time"
    , "request_text"
    , "deadline_date_time"
    , "technical_reviewer_wua_id"
    , "responded_by_wua_id"
    , "responded_date_time"
    , "response_text"
    , "response_type"
    , "technical_review_status"
    , "response_application_version_id"
    ) VALUES (
      rec.id
    , rec.request_application_version_id
    , rec.requested_by_wua_id
    , rec.requested_date_time
    , rec.request_text
    , rec.deadline_date_time
    , rec.technical_reviewer_wua_id
    , rec.responded_by_wua_id
    , rec.responded_date_time
    , rec.response_text_varchar2
    , rec.response_type
    , rec.technical_review_status
    , rec.response_application_version_id
    );
  
  END LOOP;

END;
/


--
-- file_upload_library_uploaded_files
--
BEGIN

  FOR rec IN (
    SELECT *
    FROM fcs_migration.file_upload_library_uploaded_files f
    ORDER BY to_number(usage_id), uploaded_at
  ) LOOP
  
    INSERT INTO "fcs"."file_upload_library_uploaded_files"@fcs_postgres_db (
      "id"
    , "bucket"
    , "key"
    , "name"
    , "content_type"
    , "content_length"
    , "uploaded_at"
    , "usage_id"
    , "usage_type"
    , "document_type"
    , "description"
    , "uploaded_by"
    ) VALUES (
      rec.id
    , rec.bucket
    , rec.key
    , rec.name
    , rec.content_type
    , rec.content_length
    , rec.uploaded_at
    , rec.usage_id
    , rec.usage_type
    , rec.document_type
    , rec.description
    , rec.uploaded_by
    );
  
  END LOOP;

END;
/


--
-- application_other_legacy_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.application_other_legacy_data WHERE id > 0 ORDER BY id) LOOP
  
    INSERT INTO "fcs"."application_other_legacy_data"@fcs_postgres_db (
      "id"
    , "application_version_id"
    , "increase_in_production"
    , "es_reference"
    , "uplift_percentage"
    , "field_location"
    , "previous_year_consent_history"
    , "previous_year_actuals"
    , "terminal_name"
    , "terminal_location"
    , "project_under_eia_regs"
    ) VALUES (
      rec.id
    , rec.application_version_id
    , rec.increase_in_production
    , rec.es_reference
    , rec.uplift_percentage
    , rec.field_location
    , rec.previous_year_consent_history
    , rec.previous_year_actuals
    , rec.terminal_name
    , rec.terminal_location
    , rec.project_under_eia_regs
    );
  
  END LOOP;

END;
/


--
-- split_clob_legacy_data
--
BEGIN

  FOR rec IN (SELECT * FROM fcs_migration.split_clob_legacy_data WHERE id > 0 ORDER BY id) LOOP

    INSERT INTO "fcs"."split_clob_legacy_data"@fcs_postgres_db (
      "id"
    , "source_id"
    , "source_table_name"
    , "source_column_name"
    , "text_part"
    , "text_part_index"
    ) VALUES (
      rec.id
    , rec.source_id
    , rec.source_table_name
    , rec.source_column_name
    , rec.text_part
    , rec.text_part_index
    );
  
  END LOOP;

END;
/


-- work around DB link timeout issues from sqlnet.ora param SQLNET.INBOUND_CONNECT_TIMEOUT
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/
