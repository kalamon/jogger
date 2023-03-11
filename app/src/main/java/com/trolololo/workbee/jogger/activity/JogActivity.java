package com.trolololo.workbee.jogger.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;
import com.trolololo.workbee.jogger.operations.AbstractGCodeOperationWithResult;
import com.trolololo.workbee.jogger.operations.AbstractOperation;
import com.trolololo.workbee.jogger.operations.GoToZeroOperation;
import com.trolololo.workbee.jogger.operations.HomeOperation;
import com.trolololo.workbee.jogger.operations.MoveOperation;
import com.trolololo.workbee.jogger.operations.MoveParams;
import com.trolololo.workbee.jogger.operations.ProbeZOperation;
import com.trolololo.workbee.jogger.operations.SetZeroOperation;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JogActivity extends AppCompatActivity {
    private static final String TAG = JogActivity.class.getName();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService DELAYED_EXECUTOR = Executors.newScheduledThreadPool(1);

    private Machine machine;

    private Menu menu;

    private NetworkFragment onlineStatusNetworkFragment;
    private NetworkFragment networkFragment;
    private Vibrator vibrator;
    private SharedPreferences preferences;

    private Timer timer;
    private boolean isOnline = false;

    private boolean isWorking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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
        jogger.setMoveCallback(this::runMove);
        jogger.setSetCallback(this::runSet);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onlineStatusNetworkFragment = new ViewModelProvider(this).get("online_status", NetworkFragment.class);
        onlineStatusNetworkFragment.attach(this);
        networkFragment = new ViewModelProvider(this).get(NetworkFragment.class);
        networkFragment.attach(this);

        timer = new Timer();
        testOnline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_jog, menu);
        setMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Machine.setLastOpenProfile(this, null);
            cancelAll();
            finish();
            return true;
        } else if (id == R.id.action_home) {
            return runOp(new HomeOperation(this, networkFragment, machine), "homing operation in progress, not homing again");
        } else if (id == R.id.action_gotozero) {
            return runOp(new GoToZeroOperation(this, networkFragment, machine), "Going to zero operation in progress, not doing it again again");
        } else if (id == R.id.action_probez) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.probez_proceed)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        runOp(new ProbeZOperation(this, networkFragment, machine),
                                "Probing in progress, not doing it again",
                                getString(R.string.probez_complete));
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Stopping, cancelling online ping timer");
        cancelAll();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "Restarting, rescheduling online ping timer");
        timer = new Timer();
        testOnline();
    }

    private void setMenuVisibility() {
        DELAYED_EXECUTOR.schedule(() -> {
            runOnUiThread(() -> {
                if (menu != null) {
                    boolean enabled = isOnline && !isWorking && !AbstractOperation.isInProgress();
                    MenuItem menuItem = menu.findItem(R.id.action_home);
                    menuItem.setEnabled(enabled);
                    menuItem = menu.findItem(R.id.action_gotozero);
                    menuItem.setEnabled(enabled);
                    menuItem = menu.findItem(R.id.action_probez);
                    menuItem.setEnabled(enabled);
                }
            });
        }, 100, TimeUnit.MILLISECONDS);
    }

    private boolean runOp(AbstractGCodeOperationWithResult op, String waitingLogText) {
        return runOp(op, waitingLogText, null);
    }

    private boolean runOp(AbstractGCodeOperationWithResult op, String waitingLogText, String successText) {
        if (AbstractOperation.isInProgress()) {
            return true;
        }

        op.run(new AbstractOperation.OperationCallback() {
            @Override
            public void result(Object result) {
                if (successText != null) {
                    Toast.makeText(JogActivity.this, successText, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(JogActivity.this, error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void waitForPrevious() {
                Log.w(TAG, waitingLogText);
            }

            @Override
            public void done() {
                setMenuVisibility();
            }
        });
        setMenuVisibility();
        return true;
    }

    private void runSet(Jogger.State state) {
        String[] axises = {
            getString(R.string.xy_axis),
            getString(R.string.z_axis),
            getString(R.string.xyz_axis)
        };
        SetZeroOperation.Axis[] directions = {
            SetZeroOperation.Axis.XY,
            SetZeroOperation.Axis.Z,
            SetZeroOperation.Axis.XYZ
        };
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.set_axis_title)
            .setItems(axises, (dialog, which) -> {
                Log.w(TAG, "clicked " + which);
                runOp(
                        new SetZeroOperation(this, directions[which], networkFragment, machine),
                        "Setting zero operation in progress, not doing it again",
                        String.format(getString(R.string.set_axis_result), axises[which])
                );
            })
            .show();
    }

    private void runMove(Jogger.State state) {
        if (state == null) {
            return;
        }
        MoveParams moveParams = new MoveOperation.MoveParamsBuilder()
            .setAxis(state.axis)
            .setDirection(state.direction)
            .setAmount(state.stepSize)
            .build();

        MoveOperation moveOperation = new MoveOperation(this, networkFragment, machine, moveParams);
        moveOperation.run(new AbstractOperation.OperationCallback() {
            @Override
            public void result(Object result) {
                runOnUiThread(() -> {
                    Jogger jogger = findViewById(R.id.jogger);
                    Utils.vibrate(vibrator, preferences);
                    runMove(jogger.getState());
                });
            }

            @Override
            public void error(String error) {
                Toast.makeText(JogActivity.this, error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void waitForPrevious() {
                Log.w(TAG, "move operation in progress, not moving again");
            }

            @Override
            public void done() {
                setMenuVisibility();
            }
        });
        setMenuVisibility();
    }

    private void scheduleOnlinePing() {
        if (timer == null) {
            return;
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testOnline();
            }
        }, 6000);
    }

    private void cancelAll() {
        onlineStatusNetworkFragment.cancel();
        networkFragment.cancel();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private final NetworkCall isOnlineNetworkCall = new NetworkCall() {
        public String getUrl() { return "/rr_status?type=2"; }
        public String describe() { return getString(R.string.rr_status); }
        public String fromResult(JsonElement result) {
            return null;
        }
    };

    private void testOnline() {
        EXECUTOR_SERVICE.submit(() -> {
            onlineStatusNetworkFragment.cancel();
            onlineStatusNetworkFragment.get(machine.getUrl() + isOnlineNetworkCall.getUrl(),
                null, null,
                new BaseNetworkCallback(JogActivity.this) {
                    @Override
                    public void finished() {
                        onlineStatusNetworkFragment.cancel();
                    }

                    @Override
                    public void update(JsonOp.Result result) {
                        if (result != null) {
                            if (Utils.isUnexpectedEndOfStream(result, JogActivity.this)) {
                                Log.d(TAG, "Encountered end of stream, oh well, skipping");
                            } else {
                                OnlineLabel label = findViewById(R.id.online_label);
                                isOnline = result.exception == null && result.json != null;
                                String status = getStatus(result.json);
                                isWorking = !status.equalsIgnoreCase("I") && !status.equalsIgnoreCase("B");
                                String text = isOnline
                                        ? isWorking
                                            ? getString(R.string.working)
                                            : getHomeAndCoords(result.json)
                                        : getString(R.string.offline);
                                label.setOnline(isOnline, text);
                                setMenuVisibility();
                                findViewById(R.id.cover_glass).setVisibility(isOnline && !isWorking ? View.GONE : View.VISIBLE);
                            }
                        }

                        scheduleOnlinePing();
                    }
                }
            );
        });
    }

    private String getStatus(JsonElement json) {
        JsonObject o = json.getAsJsonObject();
        JsonElement status = o.get("status");
        if (status == null) {
            throw new IllegalArgumentException("'status' property is expected");
        }
        return status.getAsString();
    }

    private String getHomeAndCoords(JsonElement json) {
        try {
            JsonObject o = json.getAsJsonObject();
            JsonElement co = o.get("coords");
            if (co == null) {
                throw new IllegalArgumentException("'coords' property is expected");
            }
            JsonObject coords = co.getAsJsonObject();
            JsonElement ah = coords.get("axesHomed");
            if (ah == null) {
                throw new IllegalArgumentException("'axesHomed' property is expected");
            }
            JsonArray axesHomed = ah.getAsJsonArray();
            boolean xHomed = axesHomed.get(0).getAsInt() > 0;
            boolean yHomed = axesHomed.get(1).getAsInt() > 0;
            boolean zHomed = axesHomed.get(2).getAsInt() > 0;
            JsonElement machine = coords.get("machine");
            if (machine == null) {
                throw new IllegalArgumentException("'machine' property is expected");
            }
            JsonArray xyz = machine.getAsJsonArray();
            float x = xyz.get(0).getAsFloat();
            float y = xyz.get(1).getAsFloat();
            float z = xyz.get(2).getAsFloat();
            boolean homed = xHomed && yHomed && zHomed;
            JsonElement speeds = o.get("speeds");
            if (speeds == null) {
                throw new IllegalArgumentException("'speeds' property is expected");
            }
            float topSpeed = speeds.getAsJsonObject().get("top").getAsFloat();
            return String.format(
                    getString(R.string.home_and_coords),
                    homed ? "Homed" : "Not homed",
                    topSpeed > 0 ? "working" : "idle",
                    x, y, z
            );
        } catch (Exception e) {
            Log.w(TAG, e);
            return getString(R.string.error);
        }
    }
}
