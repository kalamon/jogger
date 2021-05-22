package com.spartez.assettracker.checkin.actionconfig;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.adapter.FieldAdapter;
import com.spartez.assettracker.checkin.adapter.NameAndIdWrapper;
import com.spartez.assettracker.checkin.domain.Profile;
import com.spartez.assettracker.checkin.network.BaseNetworkCallback;
import com.spartez.assettracker.checkin.network.JsonOp;
import com.spartez.assettracker.checkin.network.NetworkCall;
import com.spartez.assettracker.checkin.network.NetworkFragment;

import java.util.List;

public class DateFieldActionConfig extends AbstractProfileActionConfig {
    public DateFieldActionConfig(AppCompatActivity activity, Profile profile, NetworkFragment networkFragment) {
        super(activity, profile, networkFragment);
    }

    @Override
    public void setConfig(NameAndIdWrapper field) {
        profile.setField(field != null ? field.getName() : null);
    }

    @Override
    public String validate() {
        Spinner field = (Spinner) activity.findViewById(R.id.field);
        NameAndIdWrapper selectedItem = (NameAndIdWrapper) field.getSelectedItem();
        if (selectedItem == null || selectedItem.getId() < 0) {
            return activity.getString(R.string.field_is_null);
        }
        return null;
    }

    @Override
    public void maybeRetrieveFromJira() {
        Spinner field = (Spinner) activity.findViewById(R.id.field);
        NameAndIdWrapper item = (NameAndIdWrapper) field.getAdapter().getItem(0);
        if (item.getId() > 0) {
            return;
        }

        networkFragment.cancel();
        networkFragment.get(
            profile.getUrl() + "/rest/com-spartez-ephor/1.0/fieldtype",
            profile.getLogin(),
            profile.getPassword(),
            new FieldsRetrievalNetworkCallback()
        );
    }

    @Override
    public NetworkCall addTestNetworkCall() {
        return new NetworkCall() {
            public String getUrl() { return "/rest/com-spartez-ephor/1.0/fieldtype"; }
            public String describe() { return activity.getString(R.string.at_fields); }
            public String fromResult(JsonElement result) {
                JsonArray a = result.getAsJsonArray();
                return String.format(activity.getString(R.string.field_types_result), a.size());
            }
        };
    }

    @Override
    public Spinner setupActionSelectSpinnner() {
        List<NameAndIdWrapper> fields = Lists.newArrayList();
        if (profile != null) {
            fields.add(new NameAndIdWrapper(profile.getField(), 0));
        } else {
            fields.add(new NameAndIdWrapper(activity.getString(R.string.select_field), -1));
        }
        Spinner spinner = (Spinner) activity.findViewById(R.id.field);
        activity.setTitle(R.string.date_field_profile);
        activity.findViewById(R.id.storedop_name).setVisibility(View.GONE);
        activity.findViewById(R.id.storedop).setVisibility(View.GONE);
        FieldAdapter adapter = new FieldAdapter(activity, profile, R.layout.field_selector_spinner, fields);
        adapter.setDropDownViewResource(R.layout.field_selector_spinner);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Override
    public NameAndIdWrapper getSelectedAction() {
        return (NameAndIdWrapper) ((Spinner) activity.findViewById(R.id.field)).getSelectedItem();
    }

    private void fieldsRetrieved(JsonOp.Result result) {
        List<NameAndIdWrapper> fields = Lists.newArrayList();
        if (result.json != null && result.json instanceof JsonArray) {
            JsonArray a = (JsonArray) result.json;

            int selection = 0;
            int i = 0;
            for (JsonElement element : a) {
                JsonObject o = (JsonObject) element;
                if (!Objects.equal(o.get("templateName").getAsString(), "date")) {
                    continue;
                }
                NameAndIdWrapper field = new NameAndIdWrapper(o);
                if (Objects.equal(field.getName(), profile.getField())) {
                    selection = i;
                }
                fields.add(field);
                ++i;
            }
            if (fields.size() == 0) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(activity);
                dlgAlert.setMessage(R.string.no_date_fields);
                dlgAlert.setPositiveButton(R.string.close, null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            } else {
                Spinner spinner = (Spinner) activity.findViewById(R.id.field);
                FieldAdapter adapter = (FieldAdapter) spinner.getAdapter();
                adapter.clear();
                adapter.addAll(fields);

                profile.setField(fields.get(selection).getName());
                spinner.setSelection(selection);
                adapter.notifyDataSetChanged();
            }
        } else {
            Spinner spinner = (Spinner) activity.findViewById(R.id.field);
            FieldAdapter adapter = (FieldAdapter) spinner.getAdapter();
            adapter.clear();
            adapter.add(new NameAndIdWrapper(activity.getString(R.string.select_field), -1));
            adapter.notifyDataSetChanged();
            Toast.makeText(
                    activity,
                String.format(
                    activity.getString(R.string.error_retrieving_fields),
                    result.getResultString(activity)
                ),
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private class FieldsRetrievalNetworkCallback extends BaseNetworkCallback {
        FieldsRetrievalNetworkCallback() {
            super(activity);
        }

        @Override
        public void update(JsonOp.Result result) {
            if (result != null) {
                fieldsRetrieved(result);
            }
        }

        @Override
        public void finished() {
            networkFragment.cancel();
        }
    }
}
