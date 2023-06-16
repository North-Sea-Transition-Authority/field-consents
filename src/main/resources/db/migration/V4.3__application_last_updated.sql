CREATE TABLE IF NOT EXISTS application_work_area_priorities (
    id                            SERIAL PRIMARY KEY
  , application_version_id        INTEGER NOT NULL
  , work_area_priority_group      TEXT NOT NULL
  , work_area_priority_reason     TEXT NOT NULL
  , work_area_priority_date_time  TIMESTAMP NOT NULL
  , work_area_priority_by_wua_id  INTEGER NOT NULL
  , UNIQUE (application_version_id, work_area_priority_group)
  , CONSTRAINT app_work_area_priorities_fk1_version_id
      FOREIGN KEY (application_version_id)
      REFERENCES application_versions (id)
);

CREATE INDEX app_work_area_priorities_idx1_version_id ON application_work_area_priorities(application_version_id);
