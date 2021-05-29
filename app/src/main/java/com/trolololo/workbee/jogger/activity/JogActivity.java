package com.trolololo.workbee.jogger.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.ImmutableMap;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateFinished;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateState;
import com.trolololo.workbee.jogger.assetupdate.AssetUpdateStateMachine;
import com.trolololo.workbee.jogger.domain.Profile;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class JogActivity extends AppCompatActivity {
    private static final String TAG = JogActivity.class.getName();

    private Profile profile;

    private StringBuilder logContent = new StringBuilder();
    public static final String ITEM_PREFIX = "/plugins/servlet/com.spartez.ephor/item/";
    private NetworkFragment networkFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profile = (Profile) getIntent().getSerializableExtra(Profile.class.getCanonicalName());
        Profile.setLastOpenProfile(this, profile);

        setTitle(profile.getUrl());
        setContentView(R.layout.activity_jog);

        Jogger jogger = findViewById(R.id.jogger);
        jogger.setButtons(this, new Buttons(this, ImmutableMap.<String, View>builder()
                .put(Buttons.XY, findViewById(R.id.button_x_y))
                .put(Buttons.Z, findViewById(R.id.button_z))
                .put(Buttons.SET, findViewById(R.id.button_set))
                .put(Buttons.STEP_BIG, findViewById(R.id.button_step_big))
                .put(Buttons.STEP_MEDIUM, findViewById(R.id.button_step_medium))
                .put(Buttons.STEP_SMALL, findViewById(R.id.button_step_small))
                .build()
        ));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        networkFragment = new ViewModelProvider(this).get(NetworkFragment.class);
        networkFragment.attach(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Profile.setLastOpenProfile(this, null);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doScan() {
        // continuous scan with buttons and stuff
//        Intent intent = new Intent(this, ContinuousCaptureActivity.class);
//        startActivity(intent);

        // scanner with toolbar
//        new IntentIntegrator(this).setCaptureActivity(ToolbarCaptureActivity.class).initiateScan();

//        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//        integrator.setPrompt(getString(R.string.scan_qrcode_prompt));
//        integrator.setBarcodeImageEnabled(false);
//        integrator.setOrientationLocked(false);
//        integrator.setCaptureActivity(CustomScannerActivity.class);
//        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if(result != null) {
//            String value = result.getContents();
//            if(value != null) {
//                String itemId = retrieveItemId(value);
//                if (itemId == null) {
//                    if (value.startsWith("http://") || value.startsWith("https://")) {
//                        addToLog(String.format(getString(R.string.scanned_invalid_qr_code_html), value), true);
//                    } else {
//                        addToLog(String.format(getString(R.string.scanned_invalid_qr_code_html_pre), value), true);
//                    }
//                    Toast.makeText(this, getString(R.string.scanned_invalid_qr_code_text), Toast.LENGTH_LONG).show();
//                    maybeScanAgain();
//                } else {
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    boolean forceSameJiraUrl = preferences.getBoolean("force_same_jira_url", true);
//                    if (forceSameJiraUrl && !jiraUrlMatches(value)) {
//                        String message = String.format(getString(R.string.jira_url_does_not_match), value, profile.getUrl());
//                        addToLog(message, true);
//                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//                    } else {
//                        runAssetUpdate(getAssetId(value), value);
//                    }
//                }
//            }
//        } else {
            super.onActivityResult(requestCode, resultCode, data);
//        }
    }

    private boolean jiraUrlMatches(String value) {
        return value.toLowerCase().startsWith(profile.getUrl().toLowerCase());
    }

    private String getAssetId(String url) {
        int beginIndex = url.lastIndexOf("/");
        if (beginIndex < 0 || url.length() < beginIndex + 1) {
            return "NONE";
        }
        return url.substring(beginIndex + 1);
    }

    private void runAssetUpdate(final String itemId, final String itemUrl) {
        showUpdatingMessageInLog();
        AssetUpdateStateMachine stateMachine = new AssetUpdateStateMachine(this, itemId, networkFragment, profile, new AssetUpdateFinished() {
            @Override
            public void success(AssetUpdateState state) {
                addToLog(String.format(getString(R.string.scanned_qr_code_html), itemUrl, itemId), true);
                Toast.makeText(JogActivity.this, String.format(getString(R.string.scanned_qr_code_text), itemId), Toast.LENGTH_LONG).show();
                maybeScanAgain();
            }

            @Override
            public void error(AssetUpdateState state) {
                addToLog(state.getErrorString(), true);
            }
        });
        stateMachine.run();
    }

    private void maybeScanAgain() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(JogActivity.this);
        boolean automaticContinuation = preferences.getBoolean("automatic_scan_continuation", false);
        if (automaticContinuation) {
            doScan();
        }
    }

    private void showUpdatingMessageInLog() {
//        findViewById(R.id.scan_result_log).setVisibility(View.GONE);
//        ((ProgressBar) findViewById(R.id.loadingPanelSpinner)).getIndeterminateDrawable().setColorFilter(Color.parseColor("#CECECE"), PorterDuff.Mode.SRC_ATOP);
//        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    private String retrieveItemId(String scanResult) {
        int sub = scanResult.indexOf(ITEM_PREFIX);
        if (sub < 0) {
            return null;
        }
        return scanResult.substring(sub + ITEM_PREFIX.length());
    }

    private void initLog() {
//        WebView log = findViewById(R.id.scan_result_log);
//        if (logContent.length() == 0) {
//            log.loadDataWithBaseURL(null, "<html><body>" + getString(R.string.empty_scan_log) + "</body></html>", "text/html", "utf-8", null);
//        }
    }

    private void addToLog(String text, boolean withDate) {
//        WebView log = findViewById(R.id.scan_result_log);
//        log.setVisibility(View.VISIBLE);
//        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
//        if (withDate) {
//            logContent.append(DateTimeFormat.shortDateTime().print(DateTime.now())).append(": ");
//        }
//        logContent.append(text);
//        logContent.append("<br/>");
//        log.loadDataWithBaseURL(null, "<html><body>" + logContent.toString() + "</body></html>", "text/html", "utf-8", null);
    }
}
