package com.trolololo.workbee.jogger.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.Timer;
import java.util.TimerTask;

public class JogActivity extends AppCompatActivity {
    private static final String TAG = JogActivity.class.getName();

    private Machine machine;

    private NetworkFragment onlineStatusNetworkFragment;
    private NetworkFragment networkFragment;

    private Timer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        machine = (Machine) getIntent().getSerializableExtra(Machine.class.getCanonicalName());
        Machine.setLastOpenProfile(this, machine);

        setTitle(machine.getUrl());
        setContentView(R.layout.activity_jog);

        findViewById(R.id.cover_glass).setOnClickListener(v -> {
            // choke
        });

        Jogger jogger = findViewById(R.id.jogger);
        jogger.setButtons(this, new Buttons(this, ImmutableMap.<String, View>builder()
                .put(Buttons.XY, findViewById(R.id.button_x_y))
                .put(Buttons.Z, findViewById(R.id.button_z))
                .put(Buttons.SET, findViewById(R.id.button_set))
                .put(Buttons.STEP_BIG, findViewById(R.id.button_step_big))
                .put(Buttons.STEP_MEDIUM, findViewById(R.id.button_step_medium))
                .put(Buttons.STEP_SMALL, findViewById(R.id.button_step_small))
                .build()
        ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onlineStatusNetworkFragment = new ViewModelProvider(this).get("online_status", NetworkFragment.class);
        onlineStatusNetworkFragment.attach(this);
        networkFragment = new ViewModelProvider(this).get(NetworkFragment.class);
        networkFragment.attach(this);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                testOnline();
            }
        }, 1000, 10000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Machine.setLastOpenProfile(this, null);
            cancelAll();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cancelAll() {
        onlineStatusNetworkFragment.cancel();
        networkFragment.cancel();
        timer.cancel();
    }

    private final NetworkCall isOnlineNetworkCall = new NetworkCall() {
        public String getUrl() { return "/rr_status?type=1"; }
        public String describe() { return getString(R.string.rr_status); }
        public String fromResult(JsonElement result) {
            JsonArray a = result.getAsJsonArray();
            return String.format(getString(R.string.rr_status_result), a.size());
        }
    };

    private void testOnline() {
        onlineStatusNetworkFragment.cancel();
        onlineStatusNetworkFragment.get(machine.getUrl() + isOnlineNetworkCall.getUrl(), null, null,
                new BaseNetworkCallback(JogActivity.this) {
                    @Override
                    public void finished() {
                        onlineStatusNetworkFragment.cancel();
                    }

                    @Override
                    public void update(JsonOp.Result result) {
                        if (result != null) {
                            Object resultString = result.getResultString(JogActivity.this);
                            Log.i(TAG, "Received testOnline() result: " + resultString);
                            OnlineLabel label = findViewById(R.id.online_label);
                            boolean online = result.exception == null && result.json != null;
                            String text = online ? getHomeAndCoords(result.json) : getString(R.string.offline);
                            label.setOnline(online, text);
                            findViewById(R.id.cover_glass).setVisibility(online ? View.GONE : View.VISIBLE);
                        }
                    }
                }
        );
    }

    private String getHomeAndCoords(JsonElement json) {
        try {
            JsonObject o = json.getAsJsonObject();
            JsonObject coords = o.get("coords").getAsJsonObject();
            JsonArray axesHomed = coords.get("axesHomed").getAsJsonArray();
            boolean xHomed = axesHomed.get(0).getAsInt() > 0;
            boolean yHomed = axesHomed.get(1).getAsInt() > 0;
            boolean zHomed = axesHomed.get(2).getAsInt() > 0;
            JsonArray xyz = coords.get("xyz").getAsJsonArray();
            float x = xyz.get(0).getAsFloat();
            float y = xyz.get(1).getAsFloat();
            float z = xyz.get(2).getAsFloat();
            boolean homed = xHomed && yHomed && zHomed;
            return String.format(getString(R.string.home_and_coords), homed ? "Homed" : "Not homed", x, y, z);
        } catch (Exception e) {
            Log.w(TAG, e);
            return getString(R.string.error);
        }
    }
}
