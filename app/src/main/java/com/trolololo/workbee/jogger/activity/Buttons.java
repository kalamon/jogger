package com.trolololo.workbee.jogger.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.trolololo.workbee.jogger.R;

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
    public static final String STEP_SIZE = ".stepSize.";
    public static final String IS_Z = "isZ";
    public static final String IS_BIG = "isBig";
    public static final String IS_MEDIUM = "isMedium";
    public static final String IS_SMALL = "isSmall";

    public static final Map<String, String> STEP_SIZES = Maps.newHashMap(ImmutableMap.<String, String>builder()
        .put(STEP_BIG, "50")
        .put(STEP_MEDIUM, "10")
        .put(STEP_SMALL, "0.1")
        .put(STEP_BIG + Z, "25")
        .put(STEP_MEDIUM + Z, "5")
        .put(STEP_SMALL + Z, "0.05")
        .build());

    private final SharedPreferences preferences;
    private Function<Void, Void> onZCallback;
    private Function<Void, Void> onXYCallback;
    private Function<Void, Void> onBigCallback;
    private Function<Void, Void> onMediumCallback;
    private Function<Void, Void> onSmallCallback;
    private Function<Void, Void> onStepSizeChangedCallback;
    private Function<MotionEvent, Void> onSetCallback;

    private boolean isZ = false;
    private boolean isBig = false;
    private boolean isMedium = false;
    private boolean isSmall = false;

    public Buttons(Context context, Map<String, View> buttonMap) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setInitialStepSizes();

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
        buttonBig.setOnClickListener(v -> {
            handleSize(true, false, false);
        });
        View buttonMedium = buttonMap.get(STEP_MEDIUM);
        buttonMedium.setOnClickListener(v -> {
            handleSize(false, true, false);
        });
        View buttonSmall = buttonMap.get(STEP_SMALL);
        buttonSmall.setOnClickListener(v -> {
            handleSize(false, false, true);
        });

        buttonBig.setOnLongClickListener(event -> {
            showStepSizeDialog(context, true, false, false, isZ);
            return true;
        });
        buttonMedium.setOnLongClickListener(event -> {
            showStepSizeDialog(context, false, true, false, isZ);
            return true;
        });
        buttonSmall.setOnLongClickListener(event -> {
            showStepSizeDialog(context, false, false, true, isZ);
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

    private void setInitialStepSizes() {
        String[] keys = { STEP_BIG, STEP_MEDIUM, STEP_SMALL, STEP_BIG + Z, STEP_MEDIUM + Z, STEP_SMALL + Z };

        for (String key : keys) {
            String val = load(STEP_SIZE + key, null);
            if (val != null) {
                STEP_SIZES.put(key, val);
            }
        }
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

    public void onStepSizeChanged(Function<Void, Void> f) {
        onStepSizeChangedCallback = f;
        onStepSizeChangedCallback.apply(null);
    }

    public void onSet(Function<MotionEvent, Void> f) {
        onSetCallback = f;
    }

    public boolean isZ() {
        return isZ;
    }

    public boolean isBig() {
        return isBig;
    }

    public boolean isMedium() {
        return isMedium;
    }

    public boolean isSmall() {
        return isSmall;
    }

    private boolean[] previousState = null;

    public void setSmall(boolean savePrevious) {
        if (savePrevious) {
            previousState = new boolean[]{ isBig(), isMedium(), isSmall() };
        }
        handleSize(false, false, true);
    }

    public void restoreSize() {
        if (previousState != null) {
            handleSize(previousState[0], previousState[1], previousState[2]);
            previousState = null;
        }
    }

    private void showStepSizeDialog(Context context, boolean big, boolean medium, boolean small, boolean isZ) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.step_size, null);
        String initValue = "0";
        String suffix = isZ ? Z : "";
        if (big) {
            initValue = STEP_SIZES.get(STEP_BIG + suffix);
        } else if (medium) {
            initValue = STEP_SIZES.get(STEP_MEDIUM + suffix);
        } else if (small) {
            initValue = STEP_SIZES.get(STEP_SMALL + suffix);
        }
        EditText valueInput = view.findViewById(R.id.step_size_value);
        valueInput.setText(initValue);
        if (small) {
            valueInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        new MaterialAlertDialogBuilder(context)
            .setView(view)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                EditText numberDecimal = view.findViewById(R.id.step_size_value);
                String text = numberDecimal.getText().toString();
                if (text.trim().length() > 0) {
                    Double value = Double.parseDouble(text);
                    setStepSize(big, medium, small, isZ, value);
                }
            })
            .setNeutralButton(R.string.cancel, (dialog, which) -> {})
            .show();
    }

    private void setStepSize(boolean big, boolean medium, boolean small, boolean isZ, Double value) {
        String suffix = isZ ? Z : "";
        String key = "";
        String valueString = "" + value.intValue();
        if (big) {
            key = STEP_BIG + suffix;
        } else if (medium) {
            key = STEP_MEDIUM + suffix;
        } else if (small) {
            key = STEP_SMALL + suffix;
            valueString = "" + value;
        }
        STEP_SIZES.put(key, valueString);
        save(STEP_SIZE + key, valueString);
        if (onStepSizeChangedCallback != null) {
            onStepSizeChangedCallback.apply(null);
        }
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
        String result = preferences.getString(Buttons.class.getCanonicalName()  + "." + key, defaultValue);
//        Log.i(TAG, "load " + key + ": " + result);
        return result;
    }

    private void save(String key, String value) {
//        Log.i(TAG, "save " + key + ": " + value);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Buttons.class.getCanonicalName()  + "." + key, value);
        editor.commit();
    }
}
