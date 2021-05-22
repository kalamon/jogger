package com.spartez.assettracker.checkin.assetupdate.phases;

import android.content.Context;

import com.google.gson.JsonObject;
import com.spartez.assettracker.checkin.assetupdate.AbstractAssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhaseFinished;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdateState;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public class GetDateField extends AbstractAssetUpdatePhase {
    public GetDateField(Context context, Profile profile, String success, String error) {
        super(context, success, error);
    }

    @Override
    protected void runInternal(
            NetworkFragment fragment, final Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        fragment.get(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/fieldtype/" + profile.getField(),
                profile.getLogin(), profile.getPassword(),
                new BaseNetworkCallback(getContext()) {
                    @Override
                    public void finished() {
                        if (getResult().json != null && getResult().json instanceof JsonObject) {
                            state.setFieldType((JsonObject) getResult().json);
                            callback.success(GetDateField.this, state);
                        } else {
                            state.setErrorObject(getResult().getResultString(getContext()));
                            callback.error(GetDateField.this, state);
                        }
                    }
                });

    }
}
