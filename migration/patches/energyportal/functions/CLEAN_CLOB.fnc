CREATE OR REPLACE FUNCTION fcs_migration.clean_clob (p_clob CLOB)
RETURN CLOB
AS
  l_clean_clob CLOB;
BEGIN

  -- replace multibyte characters as they cause issues with the db link push:
  -- ? and ? and £ and ‘ and ’ and – and • and ø and “ and ” and CHR(160)
  SELECT
    replace(
      replace(
        replace(
          replace(
            replace(
              replace(
                replace(
                  replace(
                    replace(
                      replace(
                        replace(p_clob, CHR(191), NULL)
                      , CHR(183), '-')
                    , CHR(163), 'GBP')
                  , CHR(145), '''')
                , CHR(146), '''')
              , CHR(150), NULL)
            , CHR(149), NULL)
          , CHR(248), NULL)
        , CHR(147), '"')
      , CHR(148), '"')
    , CHR(160), NULL) clean_clob
  INTO l_clean_clob
  FROM dual;

  RETURN l_clean_clob;
END;
/