package com.spartez.assettracker.checkin.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.spartez.assettracker.checkin.R;

public abstract class BaseNetworkCallback implements NetworkCallback {
    private final Context context;
    private JsonOp.Result result;

    public BaseNetworkCallback(Context context) {
        this.context = context;
    }

    @Override
    public void update(JsonOp.Result result) {
        this.result = result;
    }

    public JsonOp.Result getResult() {
        return result;
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode) {
        switch(progressCode) {
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }
}

