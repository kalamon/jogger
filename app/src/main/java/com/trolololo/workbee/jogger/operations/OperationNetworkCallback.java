package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public abstract class OperationNetworkCallback extends BaseNetworkCallback {
    private final NetworkFragment fragment;

    public OperationNetworkCallback(Context context, NetworkFragment fragment) {
        super(context);
        this.fragment = fragment;
    }

    protected abstract void updateInternal();

    @Override
    public void finished() {
        fragment.cancel();
    }

    @Override
    public void update(JsonOp.Result result) {
        super.update(result);
        updateInternal();
    }
}
