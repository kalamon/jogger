package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class SetZeroOperation extends AbstractGCodeOperationWithResult {
    private static final String TAG = SetZeroOperation.class.getName();

    public SetZeroOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    @Override
    protected String getGcode() {
        return null;
    }

    @Override
    public void run(OperationCallback callback) {
        callback.error("Not implemented");
    }
}
