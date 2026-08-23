package com.desktopapp.dbmanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.model.OperationResult;

public class ConnectionManager {
    
    public Connection connect(Environment env) throws SQLException{
        return DriverManager.getConnection(env.getUrl(), env.getUser(), env.getPassword());
    }

    public OperationResult testConnection(Environment env){
        try(Connection conn = connect(env)){
            if(conn != null && !conn.isClosed()){
                return OperationResult.success("Connected to " + env.getName() + " successfully.");
            }
            return OperationResult.failure("Connection to " + env.getName() + " returned no connection.");
        } catch(SQLException e){
            return OperationResult.failure("Failed to connect to " + env.getName() + ": " + e.getMessage());
        }
    }
}
