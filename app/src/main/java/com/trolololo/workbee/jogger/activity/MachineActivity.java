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
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.actionconfig.AbstractMachineActionConfig;
import com.trolololo.workbee.jogger.actionconfig.Duet3DMachineActionConfig;
import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.BaseNetworkCallback;
import com.trolololo.workbee.jogger.network.JsonOp;
import com.trolololo.workbee.jogger.network.NetworkCall;
import com.trolololo.workbee.jogger.network.NetworkFragment;

import java.util.Iterator;
import java.util.List;

public class MachineActivity extends AppCompatActivity {
    private static final String TAG = MachineActivity.class.getName();

    private Machine machine;
    private NetworkFragment networkFragment;
    private List<Object> testResults = null;
    private final List<NetworkCall> testCalls = Lists.newArrayList();

    private Iterator<NetworkCall> testCallsState;
    private NetworkCall currentTestCall;
    private Menu menu;

    private AbstractMachineActionConfig actionConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkFragment = new ViewModelProvider(this).get(NetworkFragment.class);
        networkFragment.attach(this);

        setContentView(R.layout.activity_machine);
        setTitle(R.string.edit_machine);
        EditText url = findViewById(R.id.url);

        machine = (Machine) getIntent().getSerializableExtra(Machine.class.getCanonicalName());

        if (machine != null) {
            url.setText(machine.getUrl());
        } else {
            machine = new Machine();
            url.setText("http://");
            url.setSelection(url.getText().length());
            url.setFocusableInTouchMode(true);
            url.requestFocus();
        }
        actionConfig = new Duet3DMachineActionConfig(MachineActivity.this, machine, networkFragment);
        setEventListeners(url);
        testCalls.add(actionConfig.addTestNetworkCall());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_machine, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_machine) {
            String error = validate();
            if (error == null) {
                Machine.save(getApplicationContext(), machine);
                finish();
            } else {
                Toast.makeText(MachineActivity.this, error, Toast.LENGTH_LONG).show();
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
                machine.setUrl(s.toString().trim());
            }
        });
   }

    private void finishTestingConnection() {
        if (!performNextTestCall()) {
            menu.findItem(R.id.action_test_connection).setEnabled(true);
            menu.findItem(R.id.action_save_machine).setEnabled(true);
            findViewById(R.id.url).setEnabled(true);

            Toast.makeText(MachineActivity.this, TextUtils.join("\n", testResults), Toast.LENGTH_LONG).show();
        }
    }

    private void performTestCalls() {
        menu.findItem(R.id.action_test_connection).setEnabled(false);
        menu.findItem(R.id.action_save_machine).setEnabled(false);
        findViewById(R.id.url).setEnabled(false);
        networkFragment.cancel();
        testResults = Lists.newArrayList();
        testCallsState = testCalls.iterator();
        performNextTestCall();
    }

    private boolean performNextTestCall() {
        if (testCallsState != null && testCallsState.hasNext()) {
            currentTestCall = testCallsState.next();
            networkFragment.get(
                machine.getUrl() + currentTestCall.getUrl(),
                null,
                null,
                new TestNetworkCallback()
            );
            return true;
        }
        return false;
    }

    private class TestNetworkCallback extends BaseNetworkCallback {
        TestNetworkCallback() {
            super(MachineActivity.this);
        }

        @Override
        public void update(JsonOp.Result result) {
            if (result != null) {
                if (result.json != null) {
                    testResults.add(String.format(getString(R.string.result_entry), currentTestCall.describe(), currentTestCall.fromResult(result.json)));
                } else {
                    testResults.add(String.format(getString(R.string.result_entry), currentTestCall.describe(), result.getResultString(MachineActivity.this)));
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
