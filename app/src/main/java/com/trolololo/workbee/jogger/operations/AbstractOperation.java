package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractOperation {
    private static final AtomicBoolean inProgress = new AtomicBoolean(false);

    protected static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    protected final Context context;
    protected final NetworkFragment networkFragment;
    protected final Machine machine;

    public interface OperationCallback {
        void result(Object result);
        void error(String error);
        void waitForPrevious();
        void done();
    }

    public static boolean isInProgress() {
        return inProgress.get();
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
        if (inProgress.getAndSet(true)) {
            callback.waitForPrevious();
            return;
        }
        Executors.newSingleThreadExecutor().submit(() -> {
            networkFragment.cancel();

            runInternal(result -> {
                inProgress.set(false);
                if (result.exception != null) {
                    callback.error(Utils.describeException(context, result.exception));
                } else {
                    interpretResponse(result, callback);
                }
                callback.done();
            });
        });
    }

    protected abstract void runInternal(OperationCallbackInternal callback);
    protected abstract void interpretResponse(JsonOp.Result result, OperationCallback callback);
}
