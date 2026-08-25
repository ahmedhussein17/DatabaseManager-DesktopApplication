package com.desktopapp.dbmanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.desktopapp.dbmanager.config.Environment;
import com.desktopapp.dbmanager.logging.OperationLogger;
import com.desktopapp.dbmanager.model.OperationResult;

public class ConnectionManager {
    
    public Connection connect(Environment env) throws SQLException{
        return DriverManager.getConnection(env.getUrl(), env.getUser(), env.getPassword());
    }

    public OperationResult testConnection(Environment env){
        OperationLogger logger = new OperationLogger();
        try(Connection conn = connect(env)){
            if(conn != null && !conn.isClosed()){
                String msg = "Connected to " + env.getName() + " successfully.";
                logger.log("TEST_CONNECTION", env.getName(), "-", "SUCCESS");
                return OperationResult.success(msg);
            }
            logger.log("TEST_CONNECTION", env.getName(), "-", "FAILURE - no connection returned");
            return OperationResult.failure("Connection to " + env.getName() + " returned no connection.");
        } catch(SQLException e){
            logger.log("TEST_CONNECTION", env.getName(), "-", "FAILURE - " + e.getMessage());
            return OperationResult.failure("Failed to connect to " + env.getName() + ": " + e.getMessage());
        }
    }
}
