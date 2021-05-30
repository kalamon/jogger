package com.trolololo.workbee.jogger.activity;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.trolololo.workbee.jogger.R;

public class OnlineLabel extends androidx.appcompat.widget.AppCompatTextView {
    public OnlineLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnline(boolean online, String text) {
        setBackgroundTintList(ContextCompat.getColorStateList(getContext(), online ? R.color.online : R.color.offline));
        setText(text);
    }
}
