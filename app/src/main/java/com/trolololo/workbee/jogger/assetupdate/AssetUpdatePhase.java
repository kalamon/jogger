package com.trolololo.workbee.jogger.assetupdate;

import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public interface AssetUpdatePhase {
    void run(String currentPhase, NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback);
    String getOnSuccessState();
    String getOnErrorState();
}
