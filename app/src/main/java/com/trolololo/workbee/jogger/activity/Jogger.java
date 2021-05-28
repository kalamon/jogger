package com.trolololo.workbee.jogger.activity;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.trolololo.workbee.jogger.R;

import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class Jogger extends View {
    private static final String TAG = Jogger.class.getName();

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

    private void handleMove(float x, float y) {
        Log.i(TAG, "move: " + x + ", " + y + ", " + calc(x, y, true));
    }

    private void handleUp(float x, float y) {
        Log.i(TAG, "up: " + x + ", " + y + ", " + calc(x, y, false));
        setBackground(R.drawable.ic_jog);
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
            if (newX < 0) {
                if (newX < -0.2 && Math.abs(newY) > 0.2) {
                    dir = "MINUS";
                    setBackground(newY > 0 ? R.drawable.ic_jogdown : R.drawable.ic_jogleft);
                } else {
                    setBackground(R.drawable.ic_jog);
                }
            } else {
                if (newX > 0.2 && Math.abs(newY) > 0.2) {
                    dir = "PLUS";
                    setBackground(newY > 0 ? R.drawable.ic_jogright : R.drawable.ic_jogup);
                } else {
                    setBackground(R.drawable.ic_jog);
                }
            }
        }
        return "rel: x=" + x1 + ", y=" + y1 + "rot: x=" + newX + ", y=" + newY + ", " + dir;
    }

    private void setBackground(int id) {
        setForeground(ResourcesCompat.getDrawable(getResources(), id, null));
    }
}
