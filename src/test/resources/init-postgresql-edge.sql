CREATE TABLE IF NOT EXISTS account_profile (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  email VARCHAR(128),
  mobile_phone VARCHAR(32),
  account_status SMALLINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  version INTEGER NOT NULL DEFAULT 0,
  UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS data_type_showcase (
  id BIGSERIAL PRIMARY KEY,
  c_int_value INTEGER,
  c_bigint BIGINT,
  c_numeric_value NUMERIC(20, 6),
  c_boolean_flag BOOLEAN,
  c_varchar_value VARCHAR(200),
  c_jsonb_value JSONB,
  c_bytea_value BYTEA,
  c_uuid_value UUID,
  c_xml_value XML,
  c_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS composite_pk_example (
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (tenant_id, code)
);
