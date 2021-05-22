package com.spartez.assettracker.checkin.network;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import javax.net.ssl.HttpsURLConnection;

public class NetworkFragment extends Fragment {
    public static final String TAG = "NetworkFragment";

    private JsonOp executor;
    private SharedPreferences preferences;

    public static NetworkFragment getInstance(FragmentManager fragmentManager) {
        NetworkFragment networkFragment = (NetworkFragment) fragmentManager.findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        }
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        preferences =  PreferenceManager.getDefaultSharedPreferences(context);

        if (context instanceof NetworkFragmentWatcher) {
            NetworkFragmentWatcher watcher = (NetworkFragmentWatcher) context;
            watcher.fragmentAttached(this);
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        cancel();
        super.onDestroy();
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
