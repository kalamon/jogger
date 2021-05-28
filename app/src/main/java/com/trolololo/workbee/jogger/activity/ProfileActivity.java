package com.trolololo.workbee.jogger.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.actionconfig.AbstractProfileActionConfig;
import com.trolololo.workbee.jogger.actionconfig.StoredOperationActionConfig;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.Iterator;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getName();

    private Profile profile;
    private NetworkFragment networkFragment;
    private List<Object> testResults = null;
    private final List<NetworkCall> testCalls = Lists.newArrayList(
        new NetworkCall() {
            public String getUrl() { return "/rest/api/2/serverInfo"; }
            public String describe() { return getString(R.string.jira_server_info); }
            public String fromResult(JsonElement result) {
                JsonObject o = result.getAsJsonObject();
                return String.format(getString(R.string.jira_server_info_result), o.get("version").getAsString(), o.get("serverTitle").getAsString());
            }
        },
        new NetworkCall() {
            public String getUrl() { return "/rest/com-spartez-ephor/1.0/category"; }
            public String describe() { return getString(R.string.at_folders); }
            public String fromResult(JsonElement result) {
                JsonArray a = result.getAsJsonObject().get("subcategories").getAsJsonArray();
                return String.format(getString(R.string.categories_result), a.size());
            }
        }
    );

    private Iterator<NetworkCall> testCallsState;
    private NetworkCall currentTestCall;
    private Menu menu;

    private AbstractProfileActionConfig actionConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkFragment = new ViewModelProvider(this).get(NetworkFragment.class);
        networkFragment.attach(this);

        setContentView(R.layout.activity_profile);
        setTitle(R.string.edit_profile);
        EditText url = findViewById(R.id.url);

        profile = (Profile) getIntent().getSerializableExtra(Profile.class.getCanonicalName());

        if (profile != null) {
            url.setText(profile.getUrl());
        } else {
            profile = new Profile();
            Boolean legacy = (Boolean) getIntent().getSerializableExtra(Profile.class.getCanonicalName() + ".legacy");
            url.setText("http://");
            url.setSelection(url.getText().length());
            url.setFocusableInTouchMode(true);
            url.requestFocus();
        }
        actionConfig = new StoredOperationActionConfig(ProfileActivity.this, profile, networkFragment);
        setEventListeners(url);
        testCalls.add(actionConfig.addTestNetworkCall());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_profile) {
            String error = validate();
            if (error == null) {
                Profile.save(getApplicationContext(), profile);
                finish();
            } else {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == R.id.action_test_connection) {
            performTestCalls();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String validate() {
        EditText url = findViewById(R.id.url);

        if (url.getText().length() == 0) {
            return getString(R.string.url_is_null);
        }
        return actionConfig.validate();
    }

    private void setEventListeners(TextView urlView) {
        urlView.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profile.setUrl(s.toString().trim());
            }
        });
//        fieldOrStoredOpView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                NameAndIdWrapper fieldOrOp = actionConfig.getSelectedAction();
//                if (fieldOrOp.getId() >= 0) {
//                    actionConfig.setConfig(fieldOrOp);
//                }
//            }

//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                actionConfig.setConfig(null);
//            }
//        });
//        final FloatingActionButton scan = findViewById(R.id.open);
//        scan.setOnClickListener(v -> {
//            Intent intent = new Intent(ProfileActivity.this, JogActivity.class);
//            intent.putExtra(Profile.class.getCanonicalName(), profile);
//            startActivity(intent);
//        });
//        fieldOrStoredOpView.setOnTouchListener((v, event) -> {
//            v.performClick();
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                actionConfig.maybeRetrieveFromJira();
//            }
//            return false;
//        });
    }

    private void finishTestingConnection() {
        if (!performNextTestCall()) {
            menu.findItem(R.id.action_test_connection).setEnabled(true);
            menu.findItem(R.id.action_save_profile).setEnabled(true);
            findViewById(R.id.url).setEnabled(true);
//            findViewById(R.id.login).setEnabled(true);
//            findViewById(R.id.password).setEnabled(true);
//            findViewById(R.id.field).setEnabled(true);
//            findViewById(R.id.storedop).setEnabled(true);

            Toast.makeText(ProfileActivity.this, TextUtils.join("\n", testResults), Toast.LENGTH_LONG).show();
        }
    }

    private void performTestCalls() {
        menu.findItem(R.id.action_test_connection).setEnabled(false);
        menu.findItem(R.id.action_save_profile).setEnabled(false);
        findViewById(R.id.url).setEnabled(false);
//        findViewById(R.id.login).setEnabled(false);
//        findViewById(R.id.password).setEnabled(false);
//        findViewById(R.id.field).setEnabled(false);
//        findViewById(R.id.storedop).setEnabled(false);
        networkFragment.cancel();
        testResults = Lists.newArrayList();
        testCallsState = testCalls.iterator();
        performNextTestCall();
    }

    private boolean performNextTestCall() {
        if (testCallsState != null && testCallsState.hasNext()) {
            currentTestCall = testCallsState.next();
            networkFragment.get(
                profile.getUrl() + currentTestCall.getUrl(),
                    null,
                    null,
//                profile.getLogin(),
//                profile.getPassword(),
                new TestNetworkCallback()
            );
            return true;
        }
        return false;
    }

    private class TestNetworkCallback extends BaseNetworkCallback {
        TestNetworkCallback() {
            super(ProfileActivity.this);
        }

        @Override
        public void update(JsonOp.Result result) {
            if (result != null) {
                if (result.json != null) {
                    testResults.add(String.format(getString(R.string.result_entry), currentTestCall.describe(), currentTestCall.fromResult(result.json)));
                } else {
                    testResults.add(String.format(getString(R.string.result_entry), currentTestCall.describe(), result.getResultString(ProfileActivity.this)));
                    testCallsState = null;
                    finishTestingConnection();
                }
            }
        }

        @Override
        public void finished() {
            networkFragment.cancel();
            finishTestingConnection();
        }
    }

    private static abstract class TextWatcherAdapter implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
