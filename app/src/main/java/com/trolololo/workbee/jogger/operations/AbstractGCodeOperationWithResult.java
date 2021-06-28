package com.trolololo.workbee.jogger.operations;

import android.content.Context;
import android.util.Log;

import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGCodeOperationWithResult extends AbstractOperation {
    private static final String TAG = AbstractGCodeOperationWithResult.class.getName();

    public AbstractGCodeOperationWithResult(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    protected abstract String getGcode();

    @Override
    protected void runInternal(OperationCallbackInternal callback) {
        try {
            String url = machine.getUrl() + "/rr_gcode?gcode=" + URLEncoder.encode(getGcode(), "utf-8").replace("+", "%20");
            Log.d(TAG, "GET at " + url);
            networkFragment.get(url,
                null, null,
                new OperationNetworkCallback(context, networkFragment) {
                    @Override
                    public void updateInternal() {
                        JsonOp.Result result = getResult();
                        if (result.exception != null) {
                             callback.result(result);
                        } else {
                            waitForStop(callback);
                        }
                    }
                }
            );
        } catch (UnsupportedEncodingException e) {
            callback.result(new JsonOp.Result(e));
        }
    }

    @Override
    protected void interpretResponse(JsonOp.Result result, OperationCallback callback) {
        if (result.string != null) {
            String trimmed = result.string.trim();
            if (trimmed.length() > 0) {
                callback.error(trimmed);
            }
            callback.result(result.string);
        } else {
            callback.error("Expected some sort of a string response, received nothing");
        }
    }

    private void waitForStop(OperationCallbackInternal callback) {
        executorService.schedule(() -> {
            String url = machine.getUrl() + "/rr_status?type=1";
            Log.d(TAG, "GET at " + url);
            networkFragment.get(url,
                null, null,
                new OperationNetworkCallback(context, networkFragment) {
                    @Override
                    protected void updateInternal() {
                        JsonOp.Result result = getResult();
                        if (result.exception != null) {
                            if (Utils.isUnexpectedEndOfStream(result, context)) {
                                Log.d(TAG, "Encountered end of stream, repeating waitForStop()");
                                waitForStop(callback);
                            } else {
                                callback.result(result);
                            }
                        } else {
                            float topSpeed = -1;
                            try {
                                topSpeed = result.json.getAsJsonObject().get("speeds").getAsJsonObject().get("top").getAsFloat();
                            } catch (Exception e) {
                                Log.w(TAG, e);
                                callback.result(new JsonOp.Result(e));
                            }
                            if (topSpeed > 0) {
                                waitForStop(callback);
                            } else if (topSpeed == 0){
                                readResult(callback);
                            }
                        }
                    }
                }
            );
        }, 200, TimeUnit.MILLISECONDS);
    }

    private void readResult(OperationCallbackInternal callback) {
        executorService.schedule(() -> {
            String url = machine.getUrl() + "/rr_reply";
            Log.d(TAG, "GET at " + url);
            networkFragment.get(url,
                null, null,
                new OperationNetworkCallback(context, networkFragment) {
                    @Override
                    protected void updateInternal() {
                        JsonOp.Result result = getResult();
                        if (Utils.isUnexpectedEndOfStream(result, context)) {
                            Log.d(TAG, "Encountered end of stream, repeating readResult()");
                            readResult(callback);
                        }
                        callback.result(result);
                    }
                }
            );
        }, 10, TimeUnit.MILLISECONDS);
    }
}
