CREATE OR REPLACE FUNCTION fcs_migration.clean_clob (p_text CLOB)
RETURN CLOB
AS
  l_clean_text CLOB:= p_text;
BEGIN

  FOR i IN 128..255 LOOP
    l_clean_text := replace(l_clean_text, CHR(i), '[_CHR'||i||'_]');
  END LOOP;
  
  RETURN l_clean_text;

END;
/