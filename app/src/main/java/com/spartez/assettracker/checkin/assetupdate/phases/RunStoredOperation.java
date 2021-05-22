package com.spartez.assettracker.checkin.assetupdate.phases;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.spartez.assettracker.checkin.analytics.AnalyticsService;
import com.spartez.assettracker.checkin.assetupdate.AbstractAssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhaseFinished;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdateState;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public class RunStoredOperation extends AbstractAssetUpdatePhase {
    private static final String TAG = RunStoredOperation.class.getName();

    private final String assetId;

    public RunStoredOperation(Context context, String assetId, String success, String error) {
        super(context, success, error);
        this.assetId = assetId;
    }

    @Override
    protected void runInternal(NetworkFragment fragment, final Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        fragment.post(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/stored-operation/" + profile.getOperationId() + "/execute",
                profile.getLogin(), profile.getPassword(), ImmutableList.of(assetId),
                new BaseNetworkCallback(getContext()) {
                    @Override
                    public void finished() {
                        Log.i(TAG, "finished running: json=" + getResult().json + ", string=" + getResult().string + ",exception=" + getResult().exception);
                        if (getResult().exception == null) {
                            callback.success(RunStoredOperation.this, state);
                        } else {
                            state.setErrorObject(getResult().getResultString(getContext()));
                            callback.error(RunStoredOperation.this, state);
                        }
                    }
                });
        AnalyticsService.getInstance().trackOperationSequenceExecuted(profile);

    }
}
