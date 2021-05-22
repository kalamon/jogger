package com.spartez.assettracker.checkin.assetupdate.phases;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.assetupdate.AbstractAssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhase;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdatePhaseFinished;
import com.spartez.assettracker.checkin.assetupdate.AssetUpdateState;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.NetworkFragment;

public class MaybeAddDateField extends AbstractAssetUpdatePhase {
    private final Profile profile;
    private final String assetId;

    public MaybeAddDateField(
            Context context,
            Profile profile,
            String assetId,
            String successState,
            String errorState) {
        super(context, successState, errorState);
        this.profile = profile;
        this.assetId = assetId;
    }

    @Override
    protected void runInternal(
            NetworkFragment fragment,
            final Profile profile,
            final AssetUpdateState state,
            final AssetUpdatePhaseFinished callback) {

        final AssetUpdatePhase thiz = this;
        final String assetTypeName = state.getAssetType().get("name").getAsString();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean automaticFieldCreation = preferences.getBoolean("automatic_field_creation", true);
        if (!automaticFieldCreation) {
            triggerNo(this, state, assetId, assetTypeName, callback);
            return;
        }
        final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
        dlgAlert.setMessage(String.format(getContext().getString(R.string.create_date_field), profile.getField(), assetId, assetTypeName));
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        triggerYes(thiz, state, callback);
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        triggerNo(thiz, state, assetId, assetTypeName, callback);
                        break;
                }
            }
        };
        dlgAlert.setPositiveButton(R.string.yes, dialogClickListener);
        dlgAlert.setNegativeButton(R.string.no, dialogClickListener);

        dlgAlert.setCancelable(false);
        dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                triggerNo(thiz, state, assetId, assetTypeName, callback);
            }
        });
        dlgAlert.show();
    }


    private void triggerYes(AssetUpdatePhase phase, AssetUpdateState state, AssetUpdatePhaseFinished callback) {
        callback.success(phase, state);
    }

    private void triggerNo(AssetUpdatePhase phase, AssetUpdateState state, String assetId, String assetTypeName, AssetUpdatePhaseFinished callback) {
        state.setGeneralError(String.format(getContext().getString(R.string.field_not_in_asset_type), profile.getField(), assetId, assetTypeName));
        callback.error(phase, state);
    }
}

