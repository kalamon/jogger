package com.spartez.assettracker.checkin.analytics;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.spartez.assettracker.checkin.domain.Profile;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RestApiRequest<T> extends JsonRequest<T> {
    private final Profile profile;
    private final Class<T> clazz;
    private final Gson gson;

    public RestApiRequest(Profile profile, int method, String path, String requestBody, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, profile.getUrl() + "/rest/com-spartez-ephor/1.0" + path, requestBody, listener, errorListener);
        this.gson = new Gson();
        this.clazz = clazz;
        this.profile = profile;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s", this.profile.getLogin(), this.profile.getPassword());
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        params.put("Authorization", auth);
        return params;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}
