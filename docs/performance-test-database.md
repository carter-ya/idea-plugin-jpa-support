# Performance Test Database

This repo includes local MySQL and PostgreSQL test databases for reproducing issues around large table counts and schema edge cases.

## What It Contains

- `jpa_support_perf`
  - `2200` generated tables: `perf_bulk_0001` ... `perf_bulk_2200`
  - `1` template table: `perf_template`
  - Use this database to reproduce "Select Tables" performance issues with `2000+` tables.

- `jpa_support_edge`
  - Representative tables for common field types and naming edge cases:
    - `account_profile`
    - `order_record`
    - `audit_log_entry`
    - `file_blob_asset`
    - `data_type_showcase`
    - `reserved_word_columns`
    - `prefix_user_account`
    - `composite_pk_example`
    - `flyway_schema_history`

## Start

```bash
docker compose up -d
```

## Connection

### MySQL

- Host: `127.0.0.1`
- Port: `33067`
- Username: `root`
- Password: `root`

### PostgreSQL

- Host: `127.0.0.1`
- Port: `35432`
- Username: `postgres`
- Password: `postgres`
- Database: `jpa_support_perf` or `jpa_support_edge`
- Recommended schema: `public`

## Recommended Manual Test Paths

### Reproduce Large Table Count Issue

1. Connect the plugin to MySQL `127.0.0.1:33067`
2. Choose database/schema `jpa_support_perf`
3. Open the table selection dialog
4. Confirm whether the dialog appears slowly with `2200+` tables
5. Check IDE logs for `SelectTables perf:` entries

Repeat the same flow against PostgreSQL `127.0.0.1:35432`, database `jpa_support_perf`, schema `public`.

### Verify Edge Cases

Use `jpa_support_edge` to verify:

- Decimal, date/time, JSON, blob, bit, enum, set, unsigned bigint mappings
- Reserved keyword columns
- Prefix removal behavior
- Composite primary keys
- Flyway history filtering

For PostgreSQL-specific checks, use:

- `JSONB`
- `BYTEA`
- `TIMESTAMPTZ`
- `UUID`
- `XML`

## Reset

```bash
docker compose down -v
docker compose up -d
```
