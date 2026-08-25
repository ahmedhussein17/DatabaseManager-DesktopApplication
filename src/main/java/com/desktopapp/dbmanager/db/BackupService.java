package com.desktopapp.dbmanager.db;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.logging.OperationLogger;
import com.desktopapp.dbmanager.model.OperationResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final OperationLogger logger = new OperationLogger();

    public OperationResult backup(Environment env, String tableName) {
        String backupName = tableName + "_" + LocalDate.now().format(DATE_FORMAT);

        try (Connection conn = connectionManager.connect(env)) {
            if (tableExists(conn, backupName)) {
                String msg = "Backup " + backupName + " already exists.";
                logger.log("BACKUP", env.getName(), backupName, "FAILURE - " + msg);
                return OperationResult.failure(msg);
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE " + backupName + " AS SELECT * FROM " + tableName);
            }

            String msg = "Backed up " + tableName + " as " + backupName + ".";
            logger.log("BACKUP", env.getName(), backupName, "SUCCESS");
            return OperationResult.success(msg);

        } catch (SQLException e) {
            logger.log("BACKUP", env.getName(), backupName, "FAILURE - " + e.getMessage());
            return OperationResult.failure("Backup failed: " + e.getMessage());
        }
    }

    public OperationResult restore(Environment env, String tableName, String backupName) {
        try (Connection conn = connectionManager.connect(env)) {
            if (!tableExists(conn, backupName)) {
                String msg = "Backup " + backupName + " does not exist.";
                logger.log("RESTORE", env.getName(), tableName, "FAILURE - " + msg);
                return OperationResult.failure(msg);
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS " + tableName);
                stmt.execute("CREATE TABLE " + tableName + " AS SELECT * FROM " + backupName);
            }

            String msg = "Restored " + tableName + " from " + backupName + ".";
            logger.log("RESTORE", env.getName(), tableName, "SUCCESS");
            return OperationResult.success(msg);

        } catch (SQLException e) {
            logger.log("RESTORE", env.getName(), tableName, "FAILURE - " + e.getMessage());
            return OperationResult.failure("Restore failed: " + e.getMessage());
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}