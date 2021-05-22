package com.spartez.assettracker.checkin.actionconfig;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.adapter.NameAndIdWrapper;
import com.spartez.assettracker.checkin.adapter.StoredOpAdapter;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.JsonOp;
import com.spartez.assettracker.checkin.network.NetworkCall;
import com.spartez.assettracker.checkin.network.NetworkFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoredOperationActionConfig extends AbstractProfileActionConfig {

    private static final String TAG = StoredOperationActionConfig.class.getName();

    private boolean opSetsRetrieved = false;

    public StoredOperationActionConfig(AppCompatActivity activity, Profile profile, NetworkFragment networkFragment) {
        super(activity, profile, networkFragment);
    }

    @Override
    public void setConfig(NameAndIdWrapper storedOp) {
        if (storedOp != null) {
            profile.setOperationId(storedOp.getId());
            profile.setOperationName(storedOp.getName());
        } else {
            profile.setOperationId(0);
            profile.setOperationName(null);
        }
    }

    @Override
    public String validate() {
        Spinner storedOp = (Spinner) activity.findViewById(R.id.storedop);
        NameAndIdWrapper selectedItem = (NameAndIdWrapper) storedOp.getSelectedItem();
        if (selectedItem == null || selectedItem.getId() <= 0) {
            return activity.getString(R.string.stored_op_is_null);
        }
        return null;
    }

    @Override
    public NetworkCall addTestNetworkCall() {
        return new NetworkCall() {
            public String getUrl() { return "/rest/com-spartez-ephor/1.0/stored-operation"; }
            public String describe() { return activity.getString(R.string.at_storedops); }
            public String fromResult(JsonElement result) {
                JsonArray a = result.getAsJsonArray();
                return String.format(activity.getString(R.string.storedops_result), a.size());
            }
        };
    }

    @Override
    public Spinner setupActionSelectSpinnner() {
        List<NameAndIdWrapper> storedOps = Lists.newArrayList();
        if (profile != null) {
            storedOps.add(new NameAndIdWrapper(profile.getOperationName(), profile.getOperationId()));
        } else {
            storedOps.add(new NameAndIdWrapper(activity.getString(R.string.select_storedop), -1));
        }
        Spinner spinner = (Spinner) activity.findViewById(R.id.storedop);
        activity.setTitle(R.string.stored_operation_profile);
        activity.findViewById(R.id.field_name).setVisibility(View.GONE);
        activity.findViewById(R.id.field).setVisibility(View.GONE);
        StoredOpAdapter adapter = new StoredOpAdapter(activity, profile, R.layout.field_selector_spinner, storedOps);
        adapter.setDropDownViewResource(R.layout.storedop_selector_spinner);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Override
    public NameAndIdWrapper getSelectedAction() {
        return (NameAndIdWrapper) ((Spinner) activity.findViewById(R.id.storedop)).getSelectedItem();
    }

    private void storedOpsRetrieved(JsonOp.Result result) {
        opSetsRetrieved = true;
        List<NameAndIdWrapper> storedOps = Lists.newArrayList();
        if (result.json != null && result.json instanceof JsonArray) {
            JsonArray a = (JsonArray) result.json;

            for (JsonElement element : a) {
                JsonObject o = (JsonObject) element;
                storedOps.add(new NameAndIdWrapper(o));
            }
            Collections.sort(storedOps, new Comparator<NameAndIdWrapper>() {
                @Override
                public int compare(NameAndIdWrapper o1, NameAndIdWrapper o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            int selection = 0;
            int i = 0;
            for (NameAndIdWrapper storedOp : storedOps) {
                if (storedOp.getId() == profile.getOperationId()) {
                    selection = i;
                }
                ++i;
            }
            if (storedOps.size() == 0) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(activity);
                dlgAlert.setMessage(R.string.no_storedops);
                dlgAlert.setPositiveButton(R.string.close, null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            } else {
                Spinner spinner = (Spinner) activity.findViewById(R.id.storedop);
                StoredOpAdapter adapter = (StoredOpAdapter) spinner.getAdapter();
                adapter.clear();
                adapter.addAll(storedOps);

                profile.setOperationName(storedOps.get(selection).getName());
                profile.setOperationId(storedOps.get(selection).getId());
                spinner.setSelection(selection);
                adapter.notifyDataSetChanged();
            }
        } else {
            Spinner spinner = (Spinner) activity.findViewById(R.id.storedop);
            StoredOpAdapter adapter = (StoredOpAdapter) spinner.getAdapter();
            adapter.clear();
            adapter.add(new NameAndIdWrapper(activity.getString(R.string.select_storedop), -1));
            adapter.notifyDataSetChanged();
            Toast.makeText(
                    activity,
                String.format(
                    activity.getString(R.string.error_retrieving_storedops),
                    result.getResultString(activity)
                ),
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private class StoredOpsRetrievalNetworkCallback extends BaseNetworkCallback {
        StoredOpsRetrievalNetworkCallback() {
            super(activity);
        }

        @Override
        public void update(JsonOp.Result result) {
            if (result != null) {
                storedOpsRetrieved(result);
            }
        }

        @Override
        public void finished() {
            networkFragment.cancel();
        }
    }

    @Override
    public void maybeRetrieveFromJira() {
        if (opSetsRetrieved) {
            return;
        }
        networkFragment.cancel();
        networkFragment.get(
            profile.getUrl() + "/rest/com-spartez-ephor/1.0/stored-operation",
            profile.getLogin(),
            profile.getPassword(),
            new StoredOpsRetrievalNetworkCallback()
        );
    }
}
