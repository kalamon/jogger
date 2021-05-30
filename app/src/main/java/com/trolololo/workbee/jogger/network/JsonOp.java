package com.trolololo.workbee.jogger.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonOp extends AsyncTask<String, Integer, JsonOp.Result> {
    private final String url;
    private final String user;
    private final String password;
    private final String method;
    private final Object data;
    private final int expectedCode;
    private final SharedPreferences preferences;
    private NetworkCallback callback;

    public static class Result {
        public String string;
        public JsonElement json;
        public Exception exception;

        public Result(JsonElement json) {
            this.json = json;
        }

        public Result(String string) {
            this.string = string;
        }

        public Result(Exception exception) {
            this.exception = exception;
        }

        public Object getResultString(Context context) {
            if (exception != null) {
                return Utils.describeException(context, exception);
            }
            if (json != null) {
                return json;
            }
            return string;
        }

    }

    public JsonOp(
            String url,
            String user,
            String password,
            String method,
            int expectedCode,
            NetworkCallback callback,
            SharedPreferences preferences) {
        this(url, user, password, method, null, expectedCode, callback, preferences);
    }

    public JsonOp(
            String url,
            String user,
            String password,
            String method,
            Object data,
            int expectedCode,
            NetworkCallback callback,
            SharedPreferences preferences) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.method = method;
        this.data = data;
        this.expectedCode = expectedCode;
        this.preferences = preferences;
        setCallback(callback);
    }

    public void setCallback(NetworkCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Result doInBackground(String... params) {
        Result result = null;
        if (!isCancelled()) {
            try {
                Object res = op();
                if (res instanceof JsonElement) {
                    result = new Result((JsonElement) res);
                } else if (res instanceof String) {
                    result = new Result(res.toString());
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                result = new Result(e);
            }
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        if (callback != null) {
            NetworkInfo networkInfo = callback.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                // If no connectivity, cancel task and update Callback with null data.
                callback.update(null);
                cancel(true);
            }
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (result != null && callback != null) {
            callback.update(result);
            callback.finished();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (callback != null) {
            callback.onProgressUpdate(values[0]);
        }
    }

    private int getIntParam(String key, int defaultVal) {
        try {
            String v = preferences.getString(key, "" + defaultVal);
            Integer integer = Integer.valueOf(v);
            return integer >= 0 ? integer : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Object op() throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection connection = null;
        String result = null;
        JsonElement json = null;
        try {
            boolean sendData = (method.equals("POST") || method.equals("PUT")) && (data != null);
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(1000 * getIntParam("preference_connect_timeout", 3));
            connection.setReadTimeout(1000 * getIntParam("preference_read_timeout", 3));
            connection.setRequestMethod(method);
            connection.setDoInput(true);
            if (user != null && password != null) {
                String auth = "Basic " + new String(Base64.encode((user + ":" + password).getBytes(), Base64.NO_WRAP));
                connection.setRequestProperty("Authorization", auth);
            }
            if (sendData) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoOutput(true);
            }
            connection.connect();
            if (sendData) {
                String dataJson = new Gson().toJson(data);
                outputStream = connection.getOutputStream();
                outputStream.write(dataJson.getBytes("UTF-8"));
                outputStream.flush();
            }
            publishProgress(NetworkCallback.Progress.CONNECT_SUCCESS);
            int responseCode = connection.getResponseCode();
            if (responseCode != expectedCode) {
                throw new HttpErrorException(responseCode, connection.getResponseMessage() + ". " + getErrorText(connection));
            }
            inputStream = connection.getInputStream();
            publishProgress(NetworkCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (inputStream != null) {
                result = readStream(inputStream);
                try {
                    json = new JsonParser().parse(result);
                } catch (JsonSyntaxException e) {
                    // oh well
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return json != null ? json : result;
    }

    private String getErrorText(HttpURLConnection connection) {
        InputStream inputStream = null;
        try {
            inputStream = connection.getErrorStream();
            if (inputStream != null) {
                String text = readStream(inputStream);
                return text;
            }
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }
}
