package com.trolololo.workbee.jogger.assetupdate.phases;

import android.content.Context;

import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.assetupdate.AbstractAssetUpdatePhase;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdatePhaseFinished;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateState;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class GetAsset extends AbstractAssetUpdatePhase {
    private final String assetId;

    public GetAsset(Context context, String assetId, String successState, String errorState) {
        super(context, successState, errorState);
        this.assetId = assetId;
    }

    @Override
    public void runInternal(NetworkFragment fragment, Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        fragment.get(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/item/" + assetId,
                null, null, new BaseNetworkCallback(getContext()) {
            @Override
            public void finished() {
                if (getResult().json != null && getResult().json instanceof JsonObject) {
                    state.setAsset((JsonObject) getResult().json);
                    callback.success(GetAsset.this, state);
                } else {
                    state.setErrorObject(getResult().getResultString(getContext()));
                    callback.error(GetAsset.this, state);
                }
            }
        });
    }
}
