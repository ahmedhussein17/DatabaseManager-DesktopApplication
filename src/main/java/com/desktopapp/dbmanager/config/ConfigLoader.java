package com.desktopapp.dbmanager.config;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfigLoader {

    private static final String CONFIG_PATH = "config/connections.json";

    public List<Environment> loadEnvironments(){
        
        try(FileReader reader = new FileReader(CONFIG_PATH)){
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Environment>>() {}.getType();
            List<Environment> environments = gson.fromJson(reader, listType);
            return environments != null ? environments : new ArrayList<>();
        }catch(IOException e){
            System.err.println("Could not read" + CONFIG_PATH + ": " + e.getMessage());
            return new ArrayList<>();
        }
        
    }
}
