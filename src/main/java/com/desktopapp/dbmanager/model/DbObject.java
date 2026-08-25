package com.desktopapp.dbmanager.model;

public class DbObject {

    private final String name;
    private final String type;

    public DbObject(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString(){
        return name + " (\" + type + \")";
    }
}
