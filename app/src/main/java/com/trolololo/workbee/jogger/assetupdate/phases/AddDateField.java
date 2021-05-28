package com.trolololo.workbee.jogger.assetupdate.phases;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.assetupdate.AbstractAssetUpdatePhase;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdatePhaseFinished;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateState;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class AddDateField extends AbstractAssetUpdatePhase {
    public AddDateField(Context context, String success, String error) {
        super(context, success, error);
    }

    @Override
    protected void runInternal(NetworkFragment fragment, final Profile profile, final AssetUpdateState state, final AssetUpdatePhaseFinished callback) {
        JsonElement type = state.getAssetType().get("dottedName");
        if (type == null) {
            state.setErrorObject("Asset type object does not have \"name\" property");
            callback.error(this, state);
            return;
        }
        String itemType = type.getAsString();
        fragment.post(
                profile.getUrl() + "/rest/com-spartez-ephor/1.0/itemtypefield/" + itemType,
                profile.getLogin(), profile.getPassword(), getFieldDefinition(state),
                new BaseNetworkCallback(getContext()) {
            @Override
            public void finished() {
                if (getResult().json != null && getResult().json instanceof JsonObject) {
                    state.setFieldDefinition((JsonObject) getResult().json);
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
                    dlgAlert.setTitle(R.string.must_reindex_title);
                    dlgAlert.setMessage(R.string.must_reindex);
                    dlgAlert.setPositiveButton(R.string.close, (dialog, which) -> callback.success(AddDateField.this, state));
                    dlgAlert.setCancelable(true);
                    dlgAlert.setOnCancelListener(dialog -> callback.success(AddDateField.this, state));
                    dlgAlert.create().show();
                } else {
                    state.setErrorObject(getResult().getResultString(getContext()));
                    callback.error(AddDateField.this, state);
                }
            }
        });
    }

    private Object getFieldDefinition(AssetUpdateState state) {
        return ImmutableMap.of(
            "type", state.getFieldType().get("id").getAsInt(),
            "name", state.getFieldType().get("defaultTitle").getAsString()
        );
    }
}
