package com.trolololo.workbee.jogger.assetupdate;

public interface AssetUpdatePhaseFinished {
    void success(AssetUpdatePhase phase, AssetUpdateState state);
    void error(AssetUpdatePhase phase, AssetUpdateState state);
}
