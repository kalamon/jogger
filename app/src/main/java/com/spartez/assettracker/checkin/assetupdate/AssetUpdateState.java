package com.spartez.assettracker.checkin.assetupdate;

import com.google.gson.JsonObject;

public class AssetUpdateState {
    private String generalError;
    private String currentPhase;
    private JsonObject asset;
    private Object errorObject;
    private JsonObject assetType;
    private JsonObject fieldType;
    private JsonObject fieldDefinition;

    public String getGeneralError() {
        return generalError;
    }

    public void setGeneralError(String generalError) {
        this.generalError = generalError;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void setAsset(JsonObject asset) {
        this.asset = asset;
    }

    public JsonObject getAsset() {
        return asset;
    }

    public void setErrorObject(Object errorObject) {
        this.errorObject = errorObject;
    }

    public Object getErrorObject() {
        return errorObject;
    }

    public void setAssetType(JsonObject assetType) {
        this.assetType = assetType;
    }

    public JsonObject getAssetType() {
        return assetType;
    }

    public void setFieldType(JsonObject fieldType) {
        this.fieldType = fieldType;
    }

    public JsonObject getFieldType() {
        return fieldType;
    }

    public String getErrorString() {
        StringBuilder sb = new StringBuilder();
        if (generalError != null) {
            sb.append(generalError);
        }
        if (errorObject != null) {
            sb.append(errorObject);
        }
        return sb.toString();
    }

    public void setFieldDefinition(JsonObject fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public JsonObject getFieldDefinition() {
        return fieldDefinition;
    }
}
