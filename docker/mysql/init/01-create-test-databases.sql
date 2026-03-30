CREATE DATABASE IF NOT EXISTS jpa_support_perf
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS jpa_support_edge
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE jpa_support_edge;

CREATE TABLE IF NOT EXISTS account_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  email VARCHAR(128),
  mobile_phone VARCHAR(32),
  account_status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BIT(1) NOT NULL DEFAULT b'0',
  version INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_account_profile_username (username)
) COMMENT='Typical account table';

CREATE TABLE IF NOT EXISTS order_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  total_amount DECIMAL(18, 2) NOT NULL,
  discount_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
  paid_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
  paid_at TIMESTAMP NULL DEFAULT NULL,
  remark TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_record_order_no (order_no),
  KEY idx_order_record_user_id (user_id)
) COMMENT='Order table with decimal and timestamp fields';

CREATE TABLE IF NOT EXISTS audit_log_entry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_type VARCHAR(64) NOT NULL,
  business_id BIGINT NOT NULL,
  operator_name VARCHAR(64),
  operation_result VARCHAR(32),
  before_payload JSON,
  after_payload JSON,
  ip_address VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_log_entry_business (business_type, business_id)
) COMMENT='JSON and audit fields';

CREATE TABLE IF NOT EXISTS file_blob_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(255) NOT NULL,
  file_ext VARCHAR(32),
  content_type VARCHAR(128),
  checksum CHAR(32),
  content BLOB,
  preview MEDIUMBLOB,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='Blob columns';

CREATE TABLE IF NOT EXISTS data_type_showcase (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  c_tinyint TINYINT,
  c_smallint SMALLINT,
  c_int_value INT,
  c_bigint BIGINT,
  c_bigint_unsigned BIGINT UNSIGNED,
  c_decimal_value DECIMAL(20, 6),
  c_float_value FLOAT,
  c_double_value DOUBLE,
  c_boolean_flag BOOLEAN,
  c_bitmask BIT(8),
  c_char_value CHAR(8),
  c_varchar_value VARCHAR(200),
  c_text_value TEXT,
  c_longtext_value LONGTEXT,
  c_binary_value BINARY(16),
  c_varbinary_value VARBINARY(255),
  c_blob_value BLOB,
  c_enum_value ENUM('NEW', 'PROCESSING', 'DONE'),
  c_set_value SET('A', 'B', 'C'),
  c_json_value JSON,
  c_date_value DATE,
  c_time_value TIME,
  c_datetime_value DATETIME,
  c_timestamp_value TIMESTAMP NULL DEFAULT NULL,
  c_year_value YEAR,
  c_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='MySQL common data type coverage';

CREATE TABLE IF NOT EXISTS reserved_word_columns (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  `class` VARCHAR(64),
  `default` VARCHAR(64),
  `public` VARCHAR(64),
  `package` VARCHAR(64),
  `implements` VARCHAR(64),
  `private` VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='Reserved keyword column names';

CREATE TABLE IF NOT EXISTS prefix_user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  f_user_name VARCHAR(64),
  f_user_status VARCHAR(32),
  f_created_time DATETIME,
  f_updated_time DATETIME
) COMMENT='Prefix removal testing';

CREATE TABLE IF NOT EXISTS composite_pk_example (
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  enabled TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (tenant_id, code)
) COMMENT='Composite primary key';

CREATE TABLE IF NOT EXISTS flyway_schema_history (
  installed_rank INT NOT NULL,
  version VARCHAR(50),
  description VARCHAR(200) NOT NULL,
  type VARCHAR(20) NOT NULL,
  script VARCHAR(1000) NOT NULL,
  checksum INT,
  installed_by VARCHAR(100) NOT NULL,
  installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  execution_time INT NOT NULL,
  success BOOLEAN NOT NULL,
  PRIMARY KEY (installed_rank)
) COMMENT='Flyway history table';

INSERT INTO account_profile (username, display_name, email, mobile_phone)
VALUES
  ('alice', 'Alice', 'alice@example.com', '13800000001'),
  ('bob', 'Bob', 'bob@example.com', '13800000002');

INSERT INTO order_record (user_id, order_no, total_amount, discount_amount, paid_amount, paid_at, remark)
VALUES
  (1, 'ORD-20260330-0001', 128.90, 10.00, 118.90, NOW(), 'paid'),
  (2, 'ORD-20260330-0002', 88.00, 0.00, 0.00, NULL, 'pending');

USE jpa_support_perf;

CREATE TABLE IF NOT EXISTS perf_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  external_code VARCHAR(64) NOT NULL,
  table_name VARCHAR(128) NOT NULL,
  table_status TINYINT NOT NULL DEFAULT 1,
  amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
  score DOUBLE,
  enabled BIT(1) NOT NULL DEFAULT b'1',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark VARCHAR(255),
  payload JSON,
  UNIQUE KEY uk_perf_template_external_code (external_code),
  KEY idx_perf_template_tenant_status (tenant_id, table_status)
) COMMENT='Template for bulk performance test tables';

DROP PROCEDURE IF EXISTS create_perf_tables;

DELIMITER $$

CREATE PROCEDURE create_perf_tables()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE sql_text TEXT;
  WHILE i <= 2200 DO
    SET sql_text = CONCAT(
      'CREATE TABLE IF NOT EXISTS perf_bulk_',
      LPAD(i, 4, '0'),
      ' (',
      'id BIGINT PRIMARY KEY AUTO_INCREMENT, ',
      'tenant_id BIGINT NOT NULL, ',
      'batch_no VARCHAR(64) NOT NULL, ',
      'entity_name VARCHAR(128) NOT NULL, ',
      'display_name VARCHAR(128), ',
      'status TINYINT NOT NULL DEFAULT 1, ',
      'amount DECIMAL(18,2) NOT NULL DEFAULT 0, ',
      'quantity INT NOT NULL DEFAULT 0, ',
      'active BIT(1) NOT NULL DEFAULT b''1'', ',
      'created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, ',
      'updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, ',
      'deleted_at DATETIME NULL, ',
      'payload JSON, ',
      'remark VARCHAR(255), ',
      'UNIQUE KEY uk_batch_no (batch_no), ',
      'KEY idx_tenant_status (tenant_id, status)',
      ') COMMENT=''Bulk generated performance test table #',
      i,
      ''''
    );
    SET @sql_text = sql_text;
    PREPARE stmt FROM @sql_text;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET i = i + 1;
  END WHILE;
END $$

DELIMITER ;

CALL create_perf_tables();
DROP PROCEDURE create_perf_tables;

INSERT INTO perf_template (tenant_id, external_code, table_name, amount, score, remark, payload)
VALUES
  (1, 'TPL-001', 'perf_template', 99.90, 88.5, 'seed row', JSON_OBJECT('source', 'docker-init'));
