package com.trolololo.workbee.jogger.operations;

import android.content.Context;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class ProbeZOperation extends AbstractGCodeOperationWithResult {
    public ProbeZOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    @Override
    protected String getGcode() {
        return "M400\n" +
                "G91\n" +
                "M563 P999 S\"XYZ-Probe\"\n" +
                "T999\n" +
                "M585 Z15 E3 L0 F500 S1\n" +
                "T-1\n" +
                "G10 L20 Z5\n" +
                "G1 Z5 F500\n" +
                "M500\n" +
                "G90\n" +
                "M563 P999 D-1 H-1";
    }
}
