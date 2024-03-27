DO
LANGUAGE 'plpgsql'
$$
DECLARE
    rec RECORD;
BEGIN

    FOR rec IN (
        SELECT
          sc.source_id, sc.source_table_name, sc.source_column_name
        , STRING_AGG(sc.text_part, NULL ORDER BY sc.text_part_index) text_clob
        FROM fcs.split_clob_legacy_data sc
        GROUP BY sc.source_table_name, sc.source_column_name, sc.source_id
        ORDER BY sc.source_table_name, sc.source_column_name, sc.source_id
    )
    LOOP
        EXECUTE 'UPDATE fcs.'||rec.source_table_name||' SET '||rec.source_column_name||'=$1 WHERE id=$2'
        USING rec.text_clob, rec.source_id;
    END LOOP;

END
$$
