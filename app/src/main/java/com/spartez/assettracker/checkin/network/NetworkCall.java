package com.spartez.assettracker.checkin.network;

import com.google.gson.JsonElement;

public interface NetworkCall {
    String getUrl();

    String describe();

    String fromResult(JsonElement result);
}
