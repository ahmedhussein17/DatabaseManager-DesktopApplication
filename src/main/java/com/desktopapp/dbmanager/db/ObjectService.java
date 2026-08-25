package com.desktopapp.dbmanager.db;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.model.DbObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectService {

    private final ConnectionManager connectionManager = new ConnectionManager();

    public List<DbObject> listObjects(Environment env) throws SQLException{
        List<DbObject> objects = new ArrayList<>();
        String sql = "SELECT name, type_desc FROM sys.objects " +
                     "WHERE type_desc IN ('USER_TABLE', 'VIEW', 'SQL_STORED_PROCEDURE') " +
                     "ORDER BY type_desc, name";

        try(Connection conn = connectionManager.connect(env);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while(rs.next()){
                String name = rs.getString("name");
                String typeDesc = rs.getString("type_desc");
                objects.add(new DbObject(name, mapType(typeDesc)));
            }
        }
        return objects;
    }

    public String getDefinition(Environment env, String objectName) throws SQLException{
        String sql = "SELECT OBJECT_DEFINITION(OBJECT_ID(?)) AS definition";

        try(Connection conn = connectionManager.connect(env);
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, objectName);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    String def = rs.getString("definition");
                    return def != null ? def : "(no definition available — this is likely a table, not a view or procedure)";
                }
                return "(object not found)";
            }
        }
    }

    private String mapType(String typeDesc){
        switch (typeDesc) {
            case "USER_TABLE": return "TABLE";
            case "VIEW": return "VIEW";
            case "SQL_STORED_PROCEDURE": return "PROCEDURE";
            default: return typeDesc;
        }
    }
}