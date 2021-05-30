package com.trolololo.workbee.jogger.actionconfig;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class Duet3DMachineActionConfig extends AbstractMachineActionConfig {

    private static final String TAG = Duet3DMachineActionConfig.class.getName();

    public Duet3DMachineActionConfig(AppCompatActivity activity, Machine machine, NetworkFragment networkFragment) {
        super(activity, machine, networkFragment);
    }

    @Override
    public String validate() {
        return null;
    }

    @Override
    public NetworkCall addTestNetworkCall() {
        return new NetworkCall() {
            public String getUrl() { return "/rr_status?type=2"; }
            public String describe() { return activity.getString(R.string.rr_status_2); }
            public String fromResult(JsonElement result) {
                try {
                    JsonObject o = result.getAsJsonObject();
                    return String.format(activity.getString(R.string.rr_status_extended_result),
                            o.get("name").getAsString(), o.get("firmwareName").getAsString()
                    );
                } catch (Exception e) {
                    Log.w(TAG, e);
                    return e.toString();
                }
            }
        };
    }
}
