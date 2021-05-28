package com.trolololo.workbee.jogger.actionconfig;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.adapter.FieldAdapter;
import com.trolololo.workbee.jogger.adapter.NameAndIdWrapper;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

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
        return null;
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

    private void fieldsRetrieved(JsonOp.Result result) {
//        List<NameAndIdWrapper> fields = Lists.newArrayList();
//        if (result.json instanceof JsonArray) {
//            JsonArray a = (JsonArray) result.json;
//
//            int selection = 0;
//            int i = 0;
//            for (JsonElement element : a) {
//                JsonObject o = (JsonObject) element;
//                if (!Objects.equal(o.get("templateName").getAsString(), "date")) {
//                    continue;
//                }
//                NameAndIdWrapper field = new NameAndIdWrapper(o);
//                if (Objects.equal(field.getName(), profile.getField())) {
//                    selection = i;
//                }
//                fields.add(field);
//                ++i;
//            }
//            if (fields.size() == 0) {
//                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(activity);
//                dlgAlert.setMessage(R.string.no_date_fields);
//                dlgAlert.setPositiveButton(R.string.close, null);
//                dlgAlert.setCancelable(true);
//                dlgAlert.create().show();
//            } else {
//                Spinner spinner = activity.findViewById(R.id.field);
//                FieldAdapter adapter = (FieldAdapter) spinner.getAdapter();
//                adapter.clear();
//                adapter.addAll(fields);
//
//                profile.setField(fields.get(selection).getName());
//                spinner.setSelection(selection);
//                adapter.notifyDataSetChanged();
//            }
//        } else {
//            Spinner spinner = activity.findViewById(R.id.field);
//            FieldAdapter adapter = (FieldAdapter) spinner.getAdapter();
//            adapter.clear();
//            adapter.add(new NameAndIdWrapper(activity.getString(R.string.select_field), -1));
//            adapter.notifyDataSetChanged();
//            Toast.makeText(
//                    activity,
//                String.format(
//                    activity.getString(R.string.error_retrieving_fields),
//                    result.getResultString(activity)
//                ),
//                Toast.LENGTH_LONG
//            ).show();
//        }
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
