package com.desktopapp.dbmanager.db;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.logging.OperationLogger;
import com.desktopapp.dbmanager.model.OperationResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TransferService {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final BackupService backupService = new BackupService();
    private final OperationLogger logger = new OperationLogger();

    public OperationResult transferTable(Environment sourceEnv, Environment destEnv, String tableName) {
        try (Connection sourceConn = connectionManager.connect(sourceEnv);
             Connection destConn = connectionManager.connect(destEnv)) {

            if (tableExists(destConn, tableName)) {
                OperationResult backupResult = backupService.backup(destEnv, tableName);
                if (!backupResult.isSuccess()) {
                    String msg = "Transfer aborted, destination backup failed: " + backupResult.getMessage();
                    logger.log("TRANSFER", sourceEnv.getName(), destEnv.getName(), "FAILURE - " + msg);
                    return OperationResult.failure(msg);
                }
            }

            List<ColumnInfo> columns = readColumns(sourceConn, tableName);

            try (Statement destStmt = destConn.createStatement()) {
                destStmt.execute("DROP TABLE IF EXISTS " + tableName);
                destStmt.execute(buildCreateTableSql(tableName, columns));
            }

            copyData(sourceConn, destConn, tableName, columns);

            String msg = "Transferred " + tableName + " from " + sourceEnv.getName() + " to " + destEnv.getName() + ".";
            logger.log("TRANSFER", sourceEnv.getName(), destEnv.getName(), "SUCCESS");
            return OperationResult.success(msg);

        } catch (SQLException e) {
            logger.log("TRANSFER", sourceEnv.getName(), destEnv.getName(), "FAILURE - " + e.getMessage());
            return OperationResult.failure("Transfer failed: " + e.getMessage());
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private List<ColumnInfo> readColumns(Connection conn, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " WHERE 1=0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(new ColumnInfo(meta.getColumnName(i), columnTypeDefinition(meta, i)));
            }
        }
        return columns;
    }

    private String columnTypeDefinition(ResultSetMetaData meta, int index) throws SQLException {
        String typeName = meta.getColumnTypeName(index).toLowerCase();
        int precision = meta.getPrecision(index);
        int scale = meta.getScale(index);
        
        switch (typeName) {
            case "varchar":
            case "char":
                return typeName + "(" + (precision > 0 ? precision : 255) + ")";
            case "decimal":
            case "numeric":
                return typeName + "(" + precision + "," + scale + ")";
                default:
                return typeName;
        }
    }

    private String buildCreateTableSql(String tableName, List<ColumnInfo> columns) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo col = columns.get(i);
            sql.append(col.name).append(" ").append(col.typeDefinition);
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        return sql.toString();
    }

    private void copyData(Connection sourceConn, Connection destConn, String tableName, List<ColumnInfo> columns) throws SQLException {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            placeholders.append(i == 0 ? "?" : ", ?");
        }
        String insertSql = "INSERT INTO " + tableName + " VALUES (" + placeholders + ")";

        try (Statement selectStmt = sourceConn.createStatement();
             ResultSet rs = selectStmt.executeQuery("SELECT * FROM " + tableName);
             PreparedStatement insertStmt = destConn.prepareStatement(insertSql)) {

            int columnCount = columns.size();
            int batchCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    insertStmt.setObject(i, rs.getObject(i));
                }
                insertStmt.addBatch();
                batchCount++;
                if (batchCount % 500 == 0) {
                    insertStmt.executeBatch();
                }
            }
            insertStmt.executeBatch();
        }
    }

    private static class ColumnInfo {
        final String name;
        final String typeDefinition;

        ColumnInfo(String name, String typeDefinition) {
            this.name = name;
            this.typeDefinition = typeDefinition;
        }
    }
}