package com.spartez.assettracker.checkin.assetupdate;

import android.content.Context;

import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public abstract class AbstractAssetUpdatePhase implements AssetUpdatePhase {
    private final Context context;
    private final String successState;
    private final String errorState;

    public AbstractAssetUpdatePhase(Context context, String successState, String errorState) {
        this.context = context;
        this.successState = successState;
        this.errorState = errorState;
    }

    protected void runInternal(NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback) {
        state.setGeneralError("AbstractAssetUpdatePhase.runInternal() called");
        callback.error(this, state);
    }

    @Override
    public void run(String currentPhase, NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback) {
        state.setCurrentPhase(currentPhase);
        if (fragment != null) {
            fragment.cancel();
            runInternal(fragment, profile, state, callback);
        }
    }

    @Override
    public String getOnSuccessState() {
        return successState;
    }

    @Override
    public String getOnErrorState() {
        return errorState;
    }

    public Context getContext() {
        return context;
    }
}
