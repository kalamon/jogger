package com.spartez.assettracker.checkin.assetupdate.phases;

import android.content.Context;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.spartez.assettracker.checkin.analytics.AnalyticsService;
import com.spartez.assettracker.checkin.assetupdate.AbstractAssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhaseFinished;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdateState;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.NetworkFragment;

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
        AnalyticsService.getInstance().trackDateFieldUpdate(profile);
    }
}
