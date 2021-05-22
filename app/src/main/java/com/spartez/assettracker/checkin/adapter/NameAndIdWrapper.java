package com.spartez.assettracker.checkin.adapter;

import com.google.gson.JsonObject;

public class NameAndIdWrapper {
    private String name;
    private int id;

    public NameAndIdWrapper(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public NameAndIdWrapper(JsonObject field) {
        name = field.get("name").getAsString();
        id = field.get("id").getAsInt();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
