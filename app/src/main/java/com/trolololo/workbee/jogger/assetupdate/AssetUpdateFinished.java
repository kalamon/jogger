package com.trolololo.workbee.jogger.assetupdate;

public interface AssetUpdateFinished {
    void success(AssetUpdateState state);
    void error(AssetUpdateState state);
}
