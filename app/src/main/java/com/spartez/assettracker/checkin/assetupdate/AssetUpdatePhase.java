package com.spartez.assettracker.checkin.assetupdate;

import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public interface AssetUpdatePhase {
    void run(String currentPhase, NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback);
    String getOnSuccessState();
    String getOnErrorState();
}
