package com.trolololo.workbee.jogger.actionconfig;

import android.app.Activity;
import android.widget.Spinner;

import com.trolololo.workbee.jogger.adapter.NameAndIdWrapper;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public abstract class AbstractProfileActionConfig {
    protected final Activity activity;
    protected Profile profile;
    protected NetworkFragment networkFragment;

    public AbstractProfileActionConfig(Activity activity, Profile profile, NetworkFragment networkFragment) {
        this.activity = activity;
        this.profile = profile;
        this.networkFragment = networkFragment;
    }

    public abstract void setConfig(NameAndIdWrapper fieldOrOp);

    public abstract String validate();

    public abstract NetworkCall addTestNetworkCall();
}
