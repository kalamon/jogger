package com.trolololo.workbee.jogger.network;

import com.google.gson.JsonElement;

public interface NetworkCall {
    String getUrl();

    String describe();

    String fromResult(JsonElement result);
}
