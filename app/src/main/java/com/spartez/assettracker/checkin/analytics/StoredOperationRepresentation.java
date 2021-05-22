package com.spartez.assettracker.checkin.analytics;

import java.util.ArrayList;
import java.util.List;

public class StoredOperationRepresentation {
    private int id;
    private String name;
    private boolean publicOpSet;
    private List<OperationRepresentation> operations = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicOpSet() {
        return publicOpSet;
    }

    public void setPublicOpSet(boolean publicOpSet) {
        this.publicOpSet = publicOpSet;
    }

    public List<OperationRepresentation> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationRepresentation> operations) {
        this.operations = operations;
    }
}
