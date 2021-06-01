package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class HomeOperation extends AbstractGCodeOperationWithResult {
    private static final String TAG = HomeOperation.class.getName();

    public HomeOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    @Override
    protected String getGcode() {
        return "G28";
    }
}
