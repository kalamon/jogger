package com.spartez.assettracker.checkin.assetupdate;

public interface AssetUpdatePhaseFinished {
    void success(AssetUpdatePhase phase, AssetUpdateState state);
    void error(AssetUpdatePhase phase, AssetUpdateState state);
}
