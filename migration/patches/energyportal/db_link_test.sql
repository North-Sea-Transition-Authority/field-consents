DELETE FROM "fcs"."flare_annual_123_months"@fcs_postgres_db
WHERE "id" = 999999;
/
SELECT
  "id"
, "application_version_id"
, "year"
--, "month"
, "category_1"
, "category_2"
, "category_3"
, "comments"
FROM "fcs"."flare_annual_123_months"@fcs_postgres_db
WHERE "id" = 999999;
/
BEGIN
  COMMIT;
  DBMS_SESSION.CLOSE_DATABASE_LINK('FCS_POSTGRES_DB');
END;
/

BEGIN

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
    999999
  , 1
  , 2024
  , 'JANUARY'
  , 1
  , 2
  , 3
  --, '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`!"$%^&*()-_=+[{]};:''@#~\|,<.>/?' -- these work
  , CHR(163)||CHR(172)||CHR(183)||CHR(191) -- '£¬·¿'  these don't 
  );

END;
/
SELECT CHR(163), CHR(172), CHR(183), CHR(191)
FROM dual
/