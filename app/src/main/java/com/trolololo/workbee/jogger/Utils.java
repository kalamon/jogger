package com.trolololo.workbee.jogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trolololo.workbee.jogger.network.HttpErrorException;
import com.trolololo.workbee.jogger.network.JsonOp;

public class Utils {
    private static final String TAG = Utils.class.getName();

    public static void showHelp(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setView(R.layout.help)
                .setTitle(R.string.help)
                .show();
    }

    public static void vibrate(Vibrator vibrator, SharedPreferences preferences) {
        boolean hapticsEnabled = preferences.getBoolean("preference_enable_haptic_feedback", true);
        if (hapticsEnabled) {
            try {
                vibrator.vibrate(50);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate", e);
            }
        }
    }

    public static void setBackgroundTint(Context context, View view, int color) {
        view.setBackgroundTintList(ContextCompat.getColorStateList(context, color));
    }

    public static String describeException(Context context, Exception exception) {
        if (exception instanceof HttpErrorException) {
            return String.format(context.getString(R.string.http_exception), ((HttpErrorException) exception).getErrorCode(), exception.getMessage());
        }
        return exception.getMessage();
    }

    public static boolean isUnexpectedEndOfStream(JsonOp.Result result, Context context) {
        if (result.exception != null) {
            Object resultString = result.getResultString(context);
            return resultString.equals("unexpected end of stream");
        }
        return false;
    }
}
