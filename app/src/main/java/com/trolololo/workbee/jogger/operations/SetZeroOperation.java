package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class SetZeroOperation extends AbstractGCodeOperationWithResult {
    public SetZeroOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    @Override
    protected String getGcode() {
        return "G10 L20 X0 Y0 Z0\nM500";
    }
}
