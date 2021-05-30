package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.google.gson.JsonElement;
import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.concurrent.Executors;

public abstract class AbstractOperation {
    private static boolean inProgress;

    protected final Context context;
    protected final NetworkFragment networkFragment;
    protected final Machine machine;

    public interface OperationCallback {
        void result(JsonElement result);
        void error(String error);
        void waitForPrevious();
    }

    public static boolean isInProgress() {
        return inProgress;
    }

    public AbstractOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        this.context = context;
        this.networkFragment = networkFragment;
        this.machine = machine;
    }

    protected interface OperationCallbackInternal {
        void result(JsonOp.Result result);
    }

    public void run(OperationCallback callback) {
        if (inProgress) {
            callback.waitForPrevious();
            return;
        }
        inProgress = true;
        Executors.newSingleThreadExecutor().submit(() -> {
            networkFragment.cancel();

            runInternal(result -> {
                inProgress = false;
                if (result.exception != null) {
                    callback.error(Utils.describeException(context, result.exception));
                } else {
                    callback.result(result.json);
                }
            });
        });
    }

    protected abstract void runInternal(OperationCallbackInternal callback);
}
