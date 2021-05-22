package com.spartez.assettracker.checkin.actionconfig;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Spinner;

import com.spartez.assettracker.checkin.adapter.NameAndIdWrapper;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.NetworkCall;
import com.spartez.assettracker.checkin.network.NetworkFragment;

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

    public abstract void maybeRetrieveFromJira();

    public abstract NetworkCall addTestNetworkCall();

    public abstract Spinner setupActionSelectSpinnner();

    public abstract NameAndIdWrapper getSelectedAction();
}
