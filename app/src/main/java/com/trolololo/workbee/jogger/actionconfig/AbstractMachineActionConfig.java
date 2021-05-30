package com.trolololo.workbee.jogger.actionconfig;

import android.app.Activity;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public abstract class AbstractMachineActionConfig {
    protected final Activity activity;
    protected Machine machine;
    protected NetworkFragment networkFragment;

    public AbstractMachineActionConfig(Activity activity, Machine machine, NetworkFragment networkFragment) {
        this.activity = activity;
        this.machine = machine;
        this.networkFragment = networkFragment;
    }

    public abstract String validate();

    public abstract NetworkCall addTestNetworkCall();
}
