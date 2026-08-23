package com.desktopapp.dbmanager.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationLogger {

    private static final String LOG_PATH = "logs/operations.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String operation, String source, String destination, String result){
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String line = String.format("[%s] %s | source=%s | destination=%s | result=%s" , timestamp, operation, source, destination, result);


        try(PrintWriter writer = new PrintWriter(new FileWriter(LOG_PATH, true))){
            writer.println(line);
        } catch(IOException e){
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}
