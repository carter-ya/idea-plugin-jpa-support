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

CREATE TABLE IF NOT EXISTS data_type_showcase (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  c_int_value INT,
  c_bigint BIGINT,
  c_numeric_int NUMERIC(10,0),
  c_decimal_value DECIMAL(20,6),
  c_varchar_value VARCHAR(200),
  c_boolean_flag BOOLEAN,
  c_date_value DATE,
  c_datetime_value DATETIME,
  c_timestamp_value TIMESTAMP NULL DEFAULT NULL,
  c_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='MySQL common data type coverage';

CREATE TABLE IF NOT EXISTS composite_pk_example (
  tenant_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128),
  enabled TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (tenant_id, code)
) COMMENT='Composite primary key';
