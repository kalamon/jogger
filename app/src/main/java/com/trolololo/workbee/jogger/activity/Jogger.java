package com.trolololo.workbee.jogger.activity;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.core.content.res.ResourcesCompat;

import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.Utils;
import com.trolololo.workbee.jogger.operations.AbstractMoveOperation;

import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.TOOL_TYPE_FINGER;

public class Jogger extends View {
    private static final String TAG = Jogger.class.getName();

    private Buttons buttons;
    private State state;
    private String stepSize;
    private TouchHandler moveHandler;
    private TouchHandler setHandler;

    public class State {
        public final AbstractMoveOperation.Axis axis;
        public final AbstractMoveOperation.Direction direction;
        public final String stepSize;

        public State(AbstractMoveOperation.Axis axis, AbstractMoveOperation.Direction direction, String stepSize) {
            this.axis = axis;
            this.direction = direction;
            this.stepSize = stepSize;
        }

        @Override
        public String toString() {
            return direction.toString() + axis + stepSize;
        }
    }

    public interface TouchHandler {
        void pushed(State state);
    }

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
            setStepButtonsTexts(parent, true);
            buttons.setSmall(true);
            setStepSize();
            return null;
        });
        buttons.onXY(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_x_y), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_z), R.color.trolololo);
            setBackground(R.drawable.ic_jog);
            setStepButtonsTexts(parent, false);
            setStepSize();
            buttons.restoreSize();
            return null;
        });
        buttons.onBig(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.trolololo);
            setStepSize();
            return null;
        });
        buttons.onMedium(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.colorAccent);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.trolololo);
            setStepSize();
            return null;
        });
        buttons.onSmall(foo -> {
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_big), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_medium), R.color.trolololo);
            Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_step_small), R.color.colorAccent);
            setStepSize();
            return null;
        });
        buttons.onSet(event -> {
            int action = event.getAction();
            int toolType = event.getToolType(0);
            if (toolType == TOOL_TYPE_FINGER) {
                if (action == MotionEvent.ACTION_DOWN || action == ACTION_POINTER_DOWN) {
                    Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_set), R.color.colorAccent);
                    if (setHandler != null) {
                        setHandler.pushed(state);
                    }
                    Log.i(TAG, "WORK ZERO SET");
                } else if (action == ACTION_UP || action == ACTION_POINTER_UP) {
                    Utils.setBackgroundTint(parent, parent.findViewById(R.id.button_set), R.color.trolololo);
                }
            }

            return null;
        });
    }

    private void setStepSize() {
        String suffix = buttons.isZ() ? Buttons.Z : "";
        if (buttons.isBig()) {
            stepSize = Buttons.STEP_SIZES.get(Buttons.STEP_BIG + suffix);
        } else if (buttons.isMedium()) {
            stepSize = Buttons.STEP_SIZES.get(Buttons.STEP_MEDIUM + suffix);
        } else if (buttons.isSmall()) {
            stepSize = Buttons.STEP_SIZES.get(Buttons.STEP_SMALL + suffix);
        } else {
            stepSize = "0";
        }
    }

    public State getState() {
        return state;
    }

    public void setMoveCallback(TouchHandler callback) {
        this.moveHandler = callback;
    }

    public void setSetCallback(TouchHandler callback) {
        this.setHandler = callback;
    }

    private void setStepButtonsTexts(Activity parent, boolean isZ) {
        String suffix = isZ ? Buttons.Z : "";
        String big = Buttons.STEP_SIZES.get(Buttons.STEP_BIG + suffix);
        String medium = Buttons.STEP_SIZES.get(Buttons.STEP_MEDIUM + suffix);
        String small = Buttons.STEP_SIZES.get(Buttons.STEP_SMALL + suffix);
        ((Button) parent.findViewById(R.id.button_step_big)).setText(big);
        ((Button) parent.findViewById(R.id.button_step_medium)).setText(medium);
        ((Button) parent.findViewById(R.id.button_step_small)).setText(small);
    }

    private void handleMove(float x, float y) {
        state = calc(x, y);
        Log.i(TAG, "move: " + x + ", " + y + ", " + state);
    }

    private void handleUp(float x, float y) {
        setBackground(buttons.isZ() ? R.drawable.ic_jogz : R.drawable.ic_jog);
        Log.i(TAG, "up: " + x + ", " + y);
        state = null;
    }

    private void handleDown(float x, float y) {
        state = calc(x, y);
        Log.i(TAG, "down: " + x + ", " + y + ", " + state);
        if (state != null && moveHandler != null) {
            moveHandler.pushed(state);
        }
    }

    private static final float ANGLE = 0.785398f; // 45 deg
    double COS = Math.cos(ANGLE);
    double SIN = Math.sin(ANGLE);

    private State calc(float x, float y) {
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

        AbstractMoveOperation.Axis axis = null;
        AbstractMoveOperation.Direction direction = null;

        if (buttons.isZ()) {
            axis = AbstractMoveOperation.Axis.Z;
            if (newX < 0) {
                if (newX < -0.2 && newY > 0.2) {
                    direction = AbstractMoveOperation.Direction.MINUS;
                    setBackground(R.drawable.ic_jogzdown);
                } else {
                    setBackground(R.drawable.ic_jogz);
                }
            } else {
                if (newX > 0.2 && newY < -0.2) {
                    direction = AbstractMoveOperation.Direction.PLUS;
                    setBackground(R.drawable.ic_jogzup);
                } else {
                    setBackground(R.drawable.ic_jogz);
                }
            }
        } else {
            if (newX < 0) {
                if (newX < -0.2 && Math.abs(newY) > 0.2) {
                    direction = AbstractMoveOperation.Direction.MINUS;
                    if (newY > 0) {
                        axis = AbstractMoveOperation.Axis.Y;
                        setBackground(R.drawable.ic_jogdown);
                    } else {
                        axis = AbstractMoveOperation.Axis.X;
                        setBackground(R.drawable.ic_jogleft);
                    }
                } else {
                    setBackground(R.drawable.ic_jog);
                }
            } else {
                if (newX > 0.2 && Math.abs(newY) > 0.2) {
                    direction = AbstractMoveOperation.Direction.PLUS;
                    if (newY > 0) {
                        axis = AbstractMoveOperation.Axis.X;
                        setBackground(R.drawable.ic_jogright);
                    } else {
                        axis = AbstractMoveOperation.Axis.Y;
                        setBackground(R.drawable.ic_jogup);
                    }
                } else {
                    setBackground(R.drawable.ic_jog);
                }
            }
        }

        return direction != null ? new State(axis, direction, stepSize) : null;
    }

    private void setBackground(int id) {
        setForeground(ResourcesCompat.getDrawable(getResources(), id, null));
    }
}
