package com.trolololo.workbee.jogger.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import javax.net.ssl.HttpsURLConnection;

public class NetworkFragment extends ViewModel {
    public static final String TAG = "NetworkFragment";

    private JsonOp executor;
    private SharedPreferences preferences;

    public NetworkFragment(SavedStateHandle savedStateHandle) {
    }

    public void attach(Context context) {
        preferences =  PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onCleared() {
        cancel();
    }

    public void get(String url, String user, String password, NetworkCallback callback) {
        cancel();
        executor = new JsonOp(url, user, password, "GET", HttpsURLConnection.HTTP_OK, callback, preferences);
        executor.execute();
    }

    public void post(String url, String user, String password, Object data, NetworkCallback callback) {
        cancel();
        executor = new JsonOp(url, user, password, "POST", data, HttpsURLConnection.HTTP_OK, callback, preferences);
        executor.execute();
    }

    public void cancel() {
        if (executor != null) {
            executor.cancel(true);
        }
    }
}
