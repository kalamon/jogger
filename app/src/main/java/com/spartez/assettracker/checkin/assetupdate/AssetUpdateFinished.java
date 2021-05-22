package com.spartez.assettracker.checkin.assetupdate;

public interface AssetUpdateFinished {
    void success(AssetUpdateState state);
    void error(AssetUpdateState state);
}
