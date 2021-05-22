package com.spartez.assettracker.checkin.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.zxing.common.StringUtils;
import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.domain.Profile;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

public class AnalyticsService {
    private static final String TAG = AnalyticsService.class.getName();
    private static final String ANALYTICS_UNIQUE_DEVICE_ID = "ANALYTICS_UNIQUE_DEVICE_ID";
    private static AnalyticsService instance;

    private KeenClient keenClient;
    private RequestQueue queue;
    private String uniqueDeviceId;

    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }

    public void initialize(Activity activity) {
        this.queue = Volley.newRequestQueue(activity);
        this.uniqueDeviceId = this.getUniqueDeviceId(activity);

        if (!KeenClient.isInitialized()) {
            Resources res = activity.getResources();

            KeenProject project = new KeenProject(res.getString(R.string.keen_project_id), res.getString(R.string.keen_write_key), null);

            // Create a new instance of the client.
            this.keenClient = new AndroidKeenClientBuilder(activity).build();
            this.keenClient.setDefaultProject(project);

            // Initialize the KeenClient singleton with the created client.
            KeenClient.initialize(this.keenClient);
        }
    }

    private String getUniqueDeviceId(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(ANALYTICS_UNIQUE_DEVICE_ID, Context.MODE_PRIVATE);
        String uniqueId = sharedPrefs.getString(ANALYTICS_UNIQUE_DEVICE_ID, null);
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(ANALYTICS_UNIQUE_DEVICE_ID, uniqueId);
            editor.commit();
        }
        return uniqueId;
    }

    public void trackAppStarted(long profileCount) {
        this.sendEvent("mobile app started", ImmutableMap.<String, Object>of("profileCount", profileCount));
    }

    public void trackDateFieldUpdate(Profile profile) {
        this.sendEvent("mobile date field update", ImmutableMap.<String, Object>of("fieldName", profile.getField()));
    }

    public void trackOperationSequenceExecuted(Profile profile) {
        RestApiRequest<StoredOperationRepresentation> request = new RestApiRequest<>
                (profile, Request.Method.GET, "/stored-operation/" + profile.getOperationId(), null, StoredOperationRepresentation.class, new Response.Listener<StoredOperationRepresentation>() {
                    @Override
                    public void onResponse(StoredOperationRepresentation storedOperation) {
                        if (storedOperation.getName() != null) {
                            AnalyticsService.this.sendOperationSequenceEvent(storedOperation);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, error.getMessage() != null ? error.getMessage() : "", error);
                    }
                });
        this.queue.add(request);
    }

    private void sendOperationSequenceEvent(StoredOperationRepresentation storedOperation) {
        try {
            List<String> fieldNames = new ArrayList<>();
            for (OperationRepresentation operationRepresentation : storedOperation.getOperations()) {
                fieldNames.add(operationRepresentation.getFieldName());
            }

            this.sendEvent("mobile actions", ImmutableMap.<String, Object>of(
                    "id", storedOperation.getId(),
                    "name", storedOperation.getName(),
                    "fields", TextUtils.join(",", fieldNames)
            ));
        } catch (Exception ex) {
            Log.w(TAG, ex);
        }
    }

    private Map<String, Object> getDefaultParams() {
        return ImmutableMap.<String, Object>of(
                "appInstallationId", this.uniqueDeviceId,
                "os", ImmutableMap.of(
                        "family", "Android",
                        "version", Build.VERSION.RELEASE
                )
        );
    }

    private void sendEvent(String collection, Map<String, Object> optionalParams) {
        try {
            Map<String, Object> params = new HashMap<>(this.getDefaultParams());
            if (optionalParams != null) {
                params.putAll(optionalParams);
            }
            this.keenClient.addEventAsync(collection, params);
        } catch (Exception ex) {
            Log.w(TAG, ex);
        }
    }
}
