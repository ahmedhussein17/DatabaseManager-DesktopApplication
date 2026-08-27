# Database Manager

A Java desktop application for managing databases across multiple environments (Dev, Staging, Production) — connect, back up tables, transfer data between environments, and browse database objects, all from one interface.

Built as a BRD-driven project: implemented in Java + MySQL to the same requirements.

## Features

- **Connections** — environments are defined in an external `connections.json` config file, loaded dynamically. Add or remove environments without touching code, reload them at runtime, and test any connection before using it.
- **Backup & Restore** — back up a table into a dated copy (`TableName_YYYYMMDD`), with a check to prevent duplicate same-day backups. Restore any table from an existing backup.
- **Transfer** — copy a table's structure and data from one environment to another. If the table already exists on the destination, it is automatically backed up first — the transfer aborts if that backup fails, so a destination table is never overwritten without a safety copy.
- **Database Objects** — browse tables, views, and stored procedures per environment, filter by type, and view the underlying definition (`CREATE VIEW` / `CREATE PROCEDURE`) for anything that has one.
- **Operation Logs** — every backup, restore, and transfer is logged with timestamp, operation, source, destination, and result, viewable in-app and stored in `logs/operations.log`.

## Tech Stack

- Java
- Swing + [FlatLaf](https://www.formdev.com/flatlaf/) for the UI
- MySQL via JDBC (`mysql-connector-j`)
- Gson, for reading `connections.json`
- Maven