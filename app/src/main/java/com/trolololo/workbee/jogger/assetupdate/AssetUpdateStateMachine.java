package com.trolololo.workbee.jogger.assetupdate;

import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.trolololo.workbee.jogger.assetupdate.phases.AddDateField;
import com.trolololo.workbee.jogger.assetupdate.phases.GetAsset;
import com.trolololo.workbee.jogger.assetupdate.phases.GetAssetType;
import com.trolololo.workbee.jogger.assetupdate.phases.GetDateField;
import com.trolololo.workbee.jogger.assetupdate.phases.MaybeAddDateField;
import com.trolololo.workbee.jogger.assetupdate.phases.RunStoredOperation;
import com.trolololo.workbee.jogger.assetupdate.phases.UpdateDateField;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.Map;

public class AssetUpdateStateMachine {
    private final NetworkFragment fragment;
    private final Profile profile;
    private final AssetUpdateFinished finishCallback;

    private Map<String, AssetUpdatePhase> scanStates;

    private AssetUpdatePhaseFinished callback = new AssetUpdatePhaseFinished() {
        @Override
        public void success(final AssetUpdatePhase phase, AssetUpdateState state) {
            AssetUpdatePhase nextPhase = scanStates.get(phase.getOnSuccessState());
            if (nextPhase == null) {
                state.setGeneralError("State machine is broken, no worker for state \"" + phase.getOnSuccessState() + "\"");
                finishCallback.error(state);
            } else {
                nextPhase.run(phase.getOnSuccessState(), fragment, profile, state, callback);
            }
        }

        @Override
        public void error(final AssetUpdatePhase phase, AssetUpdateState state) {
            AssetUpdatePhase nextPhase = scanStates.get(phase.getOnErrorState());
            if (nextPhase == null) {
                state.setGeneralError("State machine is broken, no worker for state \"" + phase.getOnErrorState() + "\"");
                finishCallback.error(state);
            } else {
                nextPhase.run(phase.getOnErrorState(), fragment, profile, state, callback);
            }
        }
    };

    public AssetUpdateStateMachine(
            final Context context,
            final String assetId,
            NetworkFragment fragment,
            Profile profile,
            final AssetUpdateFinished finishCallback) {
        this.fragment = fragment;
        this.profile = profile;
        this.finishCallback = finishCallback;

        if (profile.isLegacyDateFieldProfile()) {
            ImmutableMap.Builder<String, AssetUpdatePhase> builder = ImmutableMap.<String, AssetUpdatePhase>builder()
                .put("initial", new GetAsset(context, assetId, "getAssetType", "error"))
                .put("getAssetType", new GetAssetType(context, "updateDateField", "maybeAddDateField", "error"))
                .put("maybeAddDateField", new MaybeAddDateField(context, profile, assetId, "getDateField", "error"))
                .put("getDateField", new GetDateField(context, profile, "addDateField", "error"))
                .put("addDateField", new AddDateField(context, "updateDateField", "error"))
                .put("updateDateField", new UpdateDateField(context, assetId, "end", "error"));
            scanStates = addEndAndErrorStates(context, builder).build();
        } else {
            ImmutableMap.Builder<String, AssetUpdatePhase> builder = ImmutableMap.<String, AssetUpdatePhase>builder()
                .put("initial", new RunStoredOperation(context, assetId, "end", "error"));
            scanStates = addEndAndErrorStates(context, builder).build();
        }
    }

    private ImmutableMap.Builder<String, AssetUpdatePhase> addEndAndErrorStates(
            final Context context, ImmutableMap.Builder<String, AssetUpdatePhase> builder) {
        return builder
            .put("error", new AbstractAssetUpdatePhase(context, null, null) {
                @Override
                public void run(
                        String currentPhase, NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback) {
                    finishCallback.error(state);
                }
            })
            .put("end", new AbstractAssetUpdatePhase(context, null, null) {
                @Override
                public void runInternal(NetworkFragment fragment, Profile profile, AssetUpdateState state, AssetUpdatePhaseFinished callback) {
                    finishCallback.success(state);
                }
            });
    }

    public void run() {
        AssetUpdateState state = new AssetUpdateState();
        AssetUpdatePhase phase = scanStates.get("initial");
        if (phase != null) {
            phase.run("initial", fragment, profile, state, callback);
        }
    }
}
