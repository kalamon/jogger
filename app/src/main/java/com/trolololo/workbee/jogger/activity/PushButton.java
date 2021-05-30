package com.trolololo.workbee.jogger.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.material.button.MaterialButton;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.Utils;

import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class PushButton extends MaterialButton {
    public PushButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener((v, event) -> {
            performClick();
            int action = event.getAction();
            int toolType = event.getToolType(0);
            if (toolType == TOOL_TYPE_FINGER) {
                if (action == MotionEvent.ACTION_DOWN || action == ACTION_POINTER_DOWN) {
                    Utils.setBackgroundTint(getContext(), this, R.color.colorAccent);
                 } else if (action == ACTION_UP || action == ACTION_POINTER_UP) {
                    Utils.setBackgroundTint(getContext(), this, R.color.trolololo);
                }
            }
            return true;
        });
    }
}
