package com.spartez.assettracker.checkin.assetupdate.phases;

import android.content.Context;

import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spartez.assettracker.checkin.assetupdate.AbstractAssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhaseFinished;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdateState;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public class GetAssetType extends AbstractAssetUpdatePhase {
    private final String dontHaveFieldState;
    private boolean haveField = false;

    public GetAssetType(Context context, String haveFieldState, String dontHaveFieldState, String errorState) {
        super(context, haveFieldState, errorState);
        this.dontHaveFieldState = dontHaveFieldState;
    }

    @Override
    public String getOnSuccessState() {
        return haveField ? super.getOnSuccessState() : dontHaveFieldState;
    }

    @Override
    public void runInternal(NetworkFragment fragment, final Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        JsonElement type = state.getAsset().get("type");
        if (type == null) {
            state.setErrorObject("Asset object does not have \"type\" property");
            callback.error(this, state);
            return;
        }
        String itemType = type.getAsString();
        fragment.get(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/itemtype/" + itemType,
                profile.getLogin(), profile.getPassword(), new BaseNetworkCallback(getContext()) {
            @Override
            public void finished() {
                if (getResult().json != null && getResult().json instanceof JsonObject) {
                    handleResult(profile, state, (JsonObject) getResult().json);
                    callback.success(GetAssetType.this, state);
                } else {
                    state.setErrorObject(getResult().getResultString(getContext()));
                    callback.error(GetAssetType.this, state);
                }
            }
        });
    }

    private void handleResult(Profile profile, AssetUpdateState state, JsonObject result) {
        state.setAssetType(result);
        JsonElement fields = result.get("fields");
        if (fields != null) {
            JsonArray fieldsArray = fields.getAsJsonArray();
            for (JsonElement field : fieldsArray) {
                JsonObject type = field.getAsJsonObject().get("type").getAsJsonObject();
                if (type != null) {
                    JsonElement fieldName = type.get("name");
                    if (fieldName != null && Objects.equal(fieldName.getAsString(), profile.getField())) {
                        state.setFieldDefinition(field.getAsJsonObject());
                        haveField = true;
                        break;
                    }
                }
            }
        }
    }
}
