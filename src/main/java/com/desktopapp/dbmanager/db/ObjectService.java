package com.desktopapp.dbmanager.db;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.model.DbObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ObjectService {

    private final ConnectionManager connectionManager = new ConnectionManager();

    public List<DbObject> listObjects(Environment env) throws SQLException {
        List<DbObject> objects = new ArrayList<>();

        try (Connection conn = connectionManager.connect(env)) {
            String schema = conn.getCatalog();

            String tableViewSql = "SELECT table_name, table_type FROM information_schema.tables " +
                                   "WHERE table_schema = ? ORDER BY table_type, table_name";
            try (PreparedStatement stmt = conn.prepareStatement(tableViewSql)) {
                stmt.setString(1, schema);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("table_name");
                        String type = rs.getString("table_type");
                        objects.add(new DbObject(name, "VIEW".equals(type) ? "VIEW" : "TABLE"));
                    }
                }
            }

            String procSql = "SELECT routine_name FROM information_schema.routines " +
                              "WHERE routine_schema = ? AND routine_type = 'PROCEDURE' ORDER BY routine_name";
            try (PreparedStatement stmt = conn.prepareStatement(procSql)) {
                stmt.setString(1, schema);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        objects.add(new DbObject(rs.getString("routine_name"), "PROCEDURE"));
                    }
                }
            }
        }
        return objects;
    }

    public String getDefinition(Environment env, String objectName) throws SQLException {
        try (Connection conn = connectionManager.connect(env)) {
            String type = findObjectType(conn, objectName);

            if ("TABLE".equals(type)) {
                return "(no definition available — this is a table, not a view or procedure)";
            }
            if ("VIEW".equals(type)) {
                return showCreate(conn, "SHOW CREATE VIEW " + objectName, 2);
            }
            if ("PROCEDURE".equals(type)) {
                return showCreate(conn, "SHOW CREATE PROCEDURE " + objectName, 3);
            }
            return "(object not found)";
        }
    }

    private String findObjectType(Connection conn, String objectName) throws SQLException {
        String schema = conn.getCatalog();

        String tableViewSql = "SELECT table_type FROM information_schema.tables " +
                               "WHERE table_schema = ? AND table_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(tableViewSql)) {
            stmt.setString(1, schema);
            stmt.setString(2, objectName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "VIEW".equals(rs.getString("table_type")) ? "VIEW" : "TABLE";
                }
            }
        }

        String procSql = "SELECT routine_name FROM information_schema.routines " +
                          "WHERE routine_schema = ? AND routine_name = ? AND routine_type = 'PROCEDURE'";
        try (PreparedStatement stmt = conn.prepareStatement(procSql)) {
            stmt.setString(1, schema);
            stmt.setString(2, objectName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "PROCEDURE";
                }
            }
        }
        return null;
    }

    private String showCreate(Connection conn, String sql, int definitionColumnIndex) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(definitionColumnIndex);
            }
            return "(definition not found)";
        }
    }
}