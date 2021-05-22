package com.spartez.assettracker.checkin.analytics;

public class OperationRepresentation {
    private int id;
    private String fieldName;
    private String valueToSet;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValueToSet() {
        return valueToSet;
    }

    public void setValueToSet(String valueToSet) {
        this.valueToSet = valueToSet;
    }
}
