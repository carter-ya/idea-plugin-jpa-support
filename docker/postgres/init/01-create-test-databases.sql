\connect postgres

DROP DATABASE IF EXISTS jpa_support_edge;
DROP DATABASE IF EXISTS jpa_support_perf;

CREATE DATABASE jpa_support_perf;
CREATE DATABASE jpa_support_edge;

\connect jpa_support_edge

CREATE SCHEMA IF NOT EXISTS public;

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

COMMENT ON TABLE account_profile IS 'Typical account table';

CREATE TABLE IF NOT EXISTS order_record (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  total_amount NUMERIC(18, 2) NOT NULL,
  discount_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
  paid_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
  paid_at TIMESTAMP NULL,
  remark TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE order_record IS 'Order table with numeric and timestamp fields';

CREATE UNIQUE INDEX IF NOT EXISTS uk_order_record_order_no ON order_record(order_no);
CREATE INDEX IF NOT EXISTS idx_order_record_user_id ON order_record(user_id);

CREATE TABLE IF NOT EXISTS audit_log_entry (
  id BIGSERIAL PRIMARY KEY,
  business_type VARCHAR(64) NOT NULL,
  business_id BIGINT NOT NULL,
  operator_name VARCHAR(64),
  operation_result VARCHAR(32),
  before_payload JSONB,
  after_payload JSONB,
  ip_address VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE audit_log_entry IS 'JSONB and audit fields';

CREATE INDEX IF NOT EXISTS idx_audit_log_entry_business ON audit_log_entry(business_type, business_id);

CREATE TABLE IF NOT EXISTS file_blob_asset (
  id BIGSERIAL PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  file_ext VARCHAR(32),
  content_type VARCHAR(128),
  checksum CHAR(32),
  content BYTEA,
  preview BYTEA,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE file_blob_asset IS 'Binary columns';

CREATE TABLE IF NOT EXISTS data_type_showcase (
  id BIGSERIAL PRIMARY KEY,
  c_smallint SMALLINT,
  c_int_value INTEGER,
  c_bigint BIGINT,
  c_numeric_value NUMERIC(20, 6),
  c_real_value REAL,
  c_double_value DOUBLE PRECISION,
  c_boolean_flag BOOLEAN,
  c_char_value CHAR(8),
  c_varchar_value VARCHAR(200),
  c_text_value TEXT,
  c_json_value JSON,
  c_jsonb_value JSONB,
  c_bytea_value BYTEA,
  c_date_value DATE,
  c_time_value TIME,
  c_timestamptz_value TIMESTAMPTZ,
  c_uuid_value UUID,
  c_xml_value XML,
  c_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE data_type_showcase IS 'PostgreSQL common data type coverage';

CREATE TABLE IF NOT EXISTS reserved_word_columns (
  id BIGSERIAL PRIMARY KEY,
  "class" VARCHAR(64),
  "default" VARCHAR(64),
  "public" VARCHAR(64),
  "package" VARCHAR(64),
  "implements" VARCHAR(64),
  "private" VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reserved_word_columns IS 'Reserved keyword column names';

CREATE TABLE IF NOT EXISTS prefix_user_account (
  id BIGSERIAL PRIMARY KEY,
  f_user_name VARCHAR(64),
  f_user_status VARCHAR(32),
  f_created_time TIMESTAMP,
  f_updated_time TIMESTAMP
);

COMMENT ON TABLE prefix_user_account IS 'Prefix removal testing';

CREATE TABLE IF NOT EXISTS composite_pk_example (
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (tenant_id, code)
);

COMMENT ON TABLE composite_pk_example IS 'Composite primary key';

CREATE TABLE IF NOT EXISTS flyway_schema_history (
  installed_rank INTEGER PRIMARY KEY,
  version VARCHAR(50),
  description VARCHAR(200) NOT NULL,
  type VARCHAR(20) NOT NULL,
  script VARCHAR(1000) NOT NULL,
  checksum INTEGER,
  installed_by VARCHAR(100) NOT NULL,
  installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  execution_time INTEGER NOT NULL,
  success BOOLEAN NOT NULL
);

COMMENT ON TABLE flyway_schema_history IS 'Flyway history table';

INSERT INTO account_profile (username, display_name, email, mobile_phone)
VALUES
  ('alice', 'Alice', 'alice@example.com', '13800000001'),
  ('bob', 'Bob', 'bob@example.com', '13800000002')
ON CONFLICT (username) DO NOTHING;

\connect jpa_support_perf

CREATE TABLE IF NOT EXISTS perf_template (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  external_code VARCHAR(64) NOT NULL,
  table_name VARCHAR(128) NOT NULL,
  table_status SMALLINT NOT NULL DEFAULT 1,
  amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
  score DOUBLE PRECISION,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(255),
  payload JSONB
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_perf_template_external_code ON perf_template(external_code);
CREATE INDEX IF NOT EXISTS idx_perf_template_tenant_status ON perf_template(tenant_id, table_status);

SELECT format(
  'CREATE TABLE IF NOT EXISTS perf_bulk_%1$s (
     id BIGSERIAL PRIMARY KEY,
     tenant_id BIGINT NOT NULL,
     batch_no VARCHAR(64) NOT NULL,
     entity_name VARCHAR(128) NOT NULL,
     display_name VARCHAR(128),
     status SMALLINT NOT NULL DEFAULT 1,
     amount NUMERIC(18,2) NOT NULL DEFAULT 0,
     quantity INTEGER NOT NULL DEFAULT 0,
     active BOOLEAN NOT NULL DEFAULT TRUE,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     deleted_at TIMESTAMP NULL,
     payload JSONB,
     remark VARCHAR(255)
   )',
  lpad(series_id::text, 4, '0')
)
FROM generate_series(1, 2200) AS series_id
\gexec

SELECT format(
  'CREATE UNIQUE INDEX IF NOT EXISTS uk_perf_bulk_%1$s_batch_no ON perf_bulk_%1$s(batch_no)',
  lpad(series_id::text, 4, '0')
)
FROM generate_series(1, 2200) AS series_id
\gexec

SELECT format(
  'CREATE INDEX IF NOT EXISTS idx_perf_bulk_%1$s_tenant_status ON perf_bulk_%1$s(tenant_id, status)',
  lpad(series_id::text, 4, '0')
)
FROM generate_series(1, 2200) AS series_id
\gexec

SELECT format(
  'COMMENT ON TABLE perf_bulk_%1$s IS %2$L',
  lpad(series_id::text, 4, '0'),
  'Bulk generated performance test table #' || series_id
)
FROM generate_series(1, 2200) AS series_id
\gexec

INSERT INTO perf_template (tenant_id, external_code, table_name, amount, score, remark, payload)
VALUES
  (1, 'TPL-001', 'perf_template', 99.90, 88.5, 'seed row', '{"source":"docker-init"}')
ON CONFLICT (external_code) DO NOTHING;
