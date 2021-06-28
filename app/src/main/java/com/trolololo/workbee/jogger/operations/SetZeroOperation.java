package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class SetZeroOperation extends AbstractGCodeOperationWithResult {
    private final Axis axis;

    public enum Axis {
        XY("X0 Y0"),
        Z("Z0"),
        XYZ("X0 Y0 Z0");

        Axis(String gCode) {
            this.gCode = gCode;
        }

        private final String gCode;
    }

    public SetZeroOperation(Context context, Axis axis, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
        this.axis = axis;
    }

    @Override
    protected String getGcode() {
        return "G10 L20 " + axis.gCode + "\nM500";
    }
}
