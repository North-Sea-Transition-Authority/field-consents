CREATE OR REPLACE FUNCTION fcs_migration.clean_text (p_text VARCHAR2)
RETURN VARCHAR2
AS
  l_clean_text VARCHAR2(4000) := p_text;
BEGIN

  FOR i IN 128..255 LOOP
    l_clean_text := replace(l_clean_text, CHR(i), '[_CHR'||i||'_]');
  END LOOP;
  
  RETURN l_clean_text;

END;
/