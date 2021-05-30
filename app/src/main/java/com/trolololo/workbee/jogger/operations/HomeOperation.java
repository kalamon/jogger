package com.trolololo.workbee.jogger.operations;

import android.content.Context;
import android.util.Log;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class HomeOperation extends AbstractOperation {
    private static final String TAG = HomeOperation.class.getName();

    public HomeOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    @Override
    protected void runInternal(OperationCallbackInternal callback) {
        // yes, it is a GET. How lame!
        networkFragment.get(machine.getUrl() + "/rr_gcode?gcode=G28",
            null, null,
            new BaseNetworkCallback(context) {
                @Override
                public void finished() {
                }

                @Override
                public void update(JsonOp.Result result) {
                    callback.result(result);
                }
            });
    }
}
