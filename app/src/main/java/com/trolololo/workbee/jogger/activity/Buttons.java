package com.trolololo.workbee.jogger.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Function;

import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class Buttons {
    private static final String TAG = JogActivity.class.getName();

    public static final String XY = "xy";
    public static final String Z = "z";
    public static final String SET = "set";
    public static final String STEP_BIG = "step_big";
    public static final String STEP_MEDIUM = "step_medium";
    public static final String STEP_SMALL = "step_small";
    public static final String IS_Z = "isZ";
    public static final String IS_BIG = "isBig";
    public static final String IS_MEDIUM = "isMedium";
    public static final String IS_SMALL = "isSmall";

    public static final Map<String, String> STEP_SIZES = ImmutableMap.<String, String>builder()
        .put(STEP_BIG, "50")
        .put(STEP_MEDIUM, "10")
        .put(STEP_SMALL, "0.1")
        .put(STEP_BIG + Z, "25")
        .put(STEP_MEDIUM + Z, "5")
        .put(STEP_SMALL + Z, "0.05")
        .build();

    private Context context;
    private Map<String, View> buttonMap;
    private Function<Void, Void> onZCallback;
    private Function<Void, Void> onXYCallback;
    private Function<Void, Void> onBigCallback;
    private Function<Void, Void> onMediumCallback;
    private Function<Void, Void> onSmallCallback;
    private Function<MotionEvent, Void> onSetCallback;

    private boolean isZ = false;
    private boolean isBig = false;
    private boolean isMedium = false;
    private boolean isSmall = false;

    public Buttons(Context context, Map<String, View> buttonMap) {
        this.context = context;
        this.buttonMap = buttonMap;

        View buttonZ = buttonMap.get(Z);
        buttonZ.setOnTouchListener((v, event) -> {
            buttonZ.performClick();
            if (isPush(event)) {
                isZ = true;
                save(IS_Z, "true");
                if (onZCallback != null) {
                    onZCallback.apply(null);
                }
            }
            return true;
        });
        View buttonXY = buttonMap.get(XY);
        buttonXY.setOnTouchListener((v, event) -> {
            buttonXY.performClick();
            if (isPush(event)) {
                isZ = false;
                save(IS_Z, "false");
                if (onXYCallback != null) {
                    onXYCallback.apply(null);
                }
            }
            return true;
        });
        View buttonBig = buttonMap.get(STEP_BIG);
        buttonBig.setOnTouchListener((v, event) -> {
            buttonBig.performClick();
            if (isPush(event)) {
                handleSize(true, false, false);
            }
            return true;
        });
        View buttonMedium = buttonMap.get(STEP_MEDIUM);
        buttonMedium.setOnTouchListener((v, event) -> {
            buttonMedium.performClick();
            if (isPush(event)) {
                handleSize(false, true, false);
            }
            return true;
        });
        View buttonSmall = buttonMap.get(STEP_SMALL);
        buttonSmall.setOnTouchListener((v, event) -> {
            buttonSmall.performClick();
            if (isPush(event)) {
                handleSize(false, false, true);
            }
            return true;
        });

        View buttonSet = buttonMap.get(SET);
        buttonSet.setOnTouchListener((v, event) -> {
            buttonSet.performClick();
            if (onSetCallback != null) {
                onSetCallback.apply(event);
            }
            return true;
        });

        isZ = !Objects.equal(load(IS_Z, "false"), "false");
        isBig = !Objects.equal(load(IS_BIG, "true"), "false");
        isMedium = !Objects.equal(load(IS_MEDIUM, "false"), "false");
        isSmall = !Objects.equal(load(IS_SMALL, "false"), "false");
    }

    public void onZ(Function<Void, Void> f) {
        onZCallback = f;
        if (isZ) {
            onZCallback.apply(null);
        }
    }

    public void onXY(Function<Void, Void> f) {
        onXYCallback = f;
        if (!isZ) {
            onXYCallback.apply(null);
        }
    }

    public void onBig(Function<Void, Void> f) {
        onBigCallback = f;
        if (isBig) {
            onBigCallback.apply(null);
        }
    }

    public void onMedium(Function<Void, Void> f) {
        onMediumCallback = f;
        if (isMedium) {
            onMediumCallback.apply(null);
        }
    }

    public void onSmall(Function<Void, Void> f) {
        onSmallCallback = f;
        if (isSmall) {
            onSmallCallback.apply(null);
        }
    }

    public void onSet(Function<MotionEvent, Void> f) {
        onSetCallback = f;
    }

    public boolean isZ() {
        return isZ;
    }

    private void handleSize(boolean big, boolean medium, boolean small) {
        isBig = big;
        isMedium = medium;
        isSmall = small;
        save(IS_BIG, big ? "true" : "false");
        save(IS_MEDIUM, medium ? "true" : "false");
        save(IS_SMALL, small ? "true" : "false");
        if (big && onBigCallback != null) {
            onBigCallback.apply(null);
        }
        if (medium && onMediumCallback != null) {
            onMediumCallback.apply(null);
        }
        if (small && onSmallCallback != null) {
            onSmallCallback.apply(null);
        }
    }
    private boolean isPush(MotionEvent event) {
        int action = event.getAction();
        int toolType = event.getToolType(0);
        if (toolType == TOOL_TYPE_FINGER) {
            return action == MotionEvent.ACTION_DOWN || action == ACTION_POINTER_DOWN;
        }
        return false;
    }

    private String load(String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = preferences.getString(Buttons.class.getCanonicalName()  + "." + key, defaultValue);
//        Log.i(TAG, "load " + key + ": " + result);
        return result;
    }

    private void save(String key, String value) {
//        Log.i(TAG, "save " + key + ": " + value);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Buttons.class.getCanonicalName()  + "." + key, value);
        editor.commit();
    }
}
