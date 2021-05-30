package com.trolololo.workbee.jogger.activity;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.Utils;

import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class Jogger extends View {
    private static final String TAG = Jogger.class.getName();

    private Buttons buttons;

    public Jogger(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener((v, event) -> {
            performClick();
            int action = event.getAction();
            int toolType = event.getToolType(0);
            if (toolType == TOOL_TYPE_FINGER) {
                if (action == MotionEvent.ACTION_DOWN || action == ACTION_POINTER_DOWN) {
                    handleDown(event.getX(), event.getY());
                } else if (action == ACTION_UP || action == ACTION_POINTER_UP) {
                    handleUp(event.getX(), event.getY());
                } else if (action == ACTION_MOVE) {
                    handleMove(event.getX(), event.getY());
                }
            }
            return true;
        });
    }

    public void setButtons(Activity parent, Buttons buttons) {
        this.buttons = buttons;
        buttons.onZ(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_x_y), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_z), R.color.colorAccent);
            setBackground(R.drawable.ic_jogz);
            setSteps(parent, true);
            return null;
        });
        buttons.onXY(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_x_y), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_z), R.color.trolololo);
            setBackground(R.drawable.ic_jog);
            setSteps(parent, false);
            return null;
        });
        buttons.onBig(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.trolololo);
            return null;
        });
        buttons.onMedium(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.trolololo);
            return null;
        });
        buttons.onSmall(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.colorAccent);
            return null;
        });
        buttons.onSet(event -> {
            int action = event.getAction();
            int toolType = event.getToolType(0);
            if (toolType == TOOL_TYPE_FINGER) {
                if (action == MotionEvent.ACTION_DOWN || action == ACTION_POINTER_DOWN) {
                    Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_set), R.color.colorAccent);
                    Log.i(TAG, "WORK ZERO SET");
                } else if (action == ACTION_UP || action == ACTION_POINTER_UP) {
                    Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_set), R.color.trolololo);
                }
            }

            return null;
        });
    }

    private void setSteps(Activity parent, boolean isZ) {
        String suffix = isZ ? Buttons.Z : "";
        String big = Buttons.STEP_SIZES.get(Buttons.STEP_BIG + suffix);
        String medium = Buttons.STEP_SIZES.get(Buttons.STEP_MEDIUM + suffix);
        String small = Buttons.STEP_SIZES.get(Buttons.STEP_SMALL + suffix);
        ((Button) parent.findViewById(R.id.button_step_big)).setText(big);
        ((Button) parent.findViewById(R.id.button_step_medium)).setText(medium);
        ((Button) parent.findViewById(R.id.button_step_small)).setText(small);
    }

    private void handleMove(float x, float y) {
        Log.i(TAG, "move: " + x + ", " + y + ", " + calc(x, y, true));
    }

    private void handleUp(float x, float y) {
        Log.i(TAG, "up: " + x + ", " + y + ", " + calc(x, y, false));
        setBackground(buttons.isZ() ? R.drawable.ic_jogz : R.drawable.ic_jog);
    }

    private void handleDown(float x, float y) {
        Log.i(TAG, "down: " + x + ", " + y + ", " + calc(x, y, true));
    }

    private static final float ANGLE = 0.785398f; // 45 deg
    double COS = Math.cos(ANGLE);
    double SIN = Math.sin(ANGLE);

    private String calc(float x, float y, boolean setBackground) {
        float w = getWidth();
        float h = getHeight();
        float xzero = w / 2;
        float yzero = h / 2;
        float xrel = x - xzero;
        float yrel = y - yzero;
        float x1 = xrel / xzero;
        float y1 = yrel / yzero;

        double newX = x1 * COS - y1 * SIN;
        double newY = x1 * SIN + y1 * COS;

        String dir = "";
        if (setBackground) {
            if (buttons.isZ()) {
                if (newX < 0) {
                    if (newX < -0.2 && newY > 0.2) {
                        dir = "MINUS-Z";
                        setBackground(R.drawable.ic_jogzdown);
                    } else {
                        setBackground(R.drawable.ic_jogz);
                    }
                } else {
                    if (newX > 0.2 && newY < -0.2) {
                        dir = "PLUS-Z";
                        setBackground(R.drawable.ic_jogzup);
                    } else {
                        setBackground(R.drawable.ic_jogz);
                    }
                }
            } else {
                if (newX < 0) {
                    if (newX < -0.2 && Math.abs(newY) > 0.2) {
                        dir = "MINUS";
                        if (newY > 0) {
                            dir += "-Y";
                            setBackground(R.drawable.ic_jogdown);
                        } else {
                            dir += "-X";
                            setBackground(R.drawable.ic_jogleft);
                        }
                    } else {
                        setBackground(R.drawable.ic_jog);
                    }
                } else {
                    if (newX > 0.2 && Math.abs(newY) > 0.2) {
                        dir = "PLUS";
                        if (newY > 0) {
                            dir += "-X";
                            setBackground(R.drawable.ic_jogright);
                        } else {
                            dir += "-Y";
                            setBackground(R.drawable.ic_jogup);
                        }
                    } else {
                        setBackground(R.drawable.ic_jog);
                    }
                }
            }
        }
        return "rel: x=" + x1 + ", y=" + y1 + "rot: x=" + newX + ", y=" + newY + ", " + dir;
    }

    private void setBackground(int id) {
        setForeground(ResourcesCompat.getDrawable(getResources(), id, null));
    }
}
