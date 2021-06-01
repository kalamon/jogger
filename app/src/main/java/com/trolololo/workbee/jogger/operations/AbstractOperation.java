package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractOperation {
    private static boolean inProgress;

    protected static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    protected final Context context;
    protected final NetworkFragment networkFragment;
    protected final Machine machine;

    public interface OperationCallback {
        void result(Object result);
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
                    interpretResponse(result, callback);
                }
            });
        });
    }

    protected abstract void runInternal(OperationCallbackInternal callback);
    protected abstract void interpretResponse(JsonOp.Result result, OperationCallback callback);
}
