package com.trolololo.workbee.jogger;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.trolololo.workbee.jogger.network.HttpErrorException;
import com.trolololo.workbee.jogger.network.JsonOp;

public class Utils {
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
