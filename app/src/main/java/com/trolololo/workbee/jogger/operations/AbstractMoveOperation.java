package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMoveOperation extends AbstractOperation {
    public AbstractMoveOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    protected abstract long getWaitTime();

    protected abstract String getGcode();

    @Override
    protected void runInternal(OperationCallbackInternal callback) {
        try {
            networkFragment.get(machine.getUrl() + "/rr_gcode?gcode=" + URLEncoder.encode(getGcode(), "utf-8"),
            null, null,
            new OperationNetworkCallback(context, networkFragment) {
                @Override
                public void updateInternal() {
                    Executors.newScheduledThreadPool(1).schedule(() -> {
                        JsonOp.Result result = getResult();
                        if (result.exception != null) {
                            callback.result(result);
                        } else {
                            // TODO - check result?
                            getOperationStatus(callback);
                        }
                    }, getWaitTime(), TimeUnit.MILLISECONDS);
                }
            });
        } catch (UnsupportedEncodingException e) {
            callback.result(new JsonOp.Result(e));
        }
    }

    private void getOperationStatus(OperationCallbackInternal callback) {
        networkFragment.get(machine.getUrl() + "/rr_result",
            null, null,
            new OperationNetworkCallback(context, networkFragment) {
                @Override
                protected void updateInternal() {
                    // TODO check result again?
                    callback.result(getResult());
                }
            }
        );
   }
}
