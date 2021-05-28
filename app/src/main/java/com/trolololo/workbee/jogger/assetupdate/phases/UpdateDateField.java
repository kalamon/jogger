package com.trolololo.workbee.jogger.assetupdate.phases;

import android.content.Context;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.assetupdate.AbstractAssetUpdatePhase;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdatePhaseFinished;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateState;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import org.joda.time.DateTime;

public class UpdateDateField extends AbstractAssetUpdatePhase {
    private final String assetId;

    public UpdateDateField(Context context, String assetId, String success, String error) {
        super(context, success, error);
        this.assetId = assetId;
    }

    @Override
    protected void runInternal(NetworkFragment fragment, final Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        fragment.post(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/item/" + assetId + "/field/-1?fielddef=" + state.getFieldDefinition().get("id").getAsInt(),
                profile.getLogin(), profile.getPassword(), ImmutableList.of(DateTime.now().toDate().getTime()),
                new BaseNetworkCallback(getContext()) {
                    @Override
                    public void finished() {
                        if (getResult().json != null && getResult().json instanceof JsonObject) {
                            callback.success(UpdateDateField.this, state);
                        } else {
                            state.setErrorObject(getResult().getResultString(getContext()));
                            callback.error(UpdateDateField.this, state);
                        }
                    }
                });
    }
}
