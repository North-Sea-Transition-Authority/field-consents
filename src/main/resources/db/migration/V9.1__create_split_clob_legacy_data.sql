CREATE TABLE split_clob_legacy_data (
  id                 SERIAL PRIMARY KEY
, source_id          INTEGER NOT NULL
, source_table_name  TEXT NOT NULL
, source_column_name TEXT NOT NULL
, text_part          TEXT NOT NULL
, text_part_index    INTEGER NOT NULL
);
