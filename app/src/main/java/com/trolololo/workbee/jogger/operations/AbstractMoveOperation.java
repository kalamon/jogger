package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public abstract class AbstractMoveOperation extends AbstractGCodeOperationWithResult {
    public enum Axis {
        X("X"),
        Y("Y"),
        Z("Z");

        private final String axis;

        Axis(String axis) {
            this.axis = axis;
        }

        @Override
        public String toString() {
            return axis;
        }
    }

    public enum Direction {
        PLUS(""),
        MINUS("-");

        private String direction;

        Direction(String direction) {
            this.direction = direction;
        }

        @Override
        public String toString() {
            return direction;
        }
    }

    public AbstractMoveOperation(Context context, NetworkFragment networkFragment, Machine machine) {
        super(context, networkFragment, machine);
    }

    protected abstract Axis getAxis();
    protected abstract Direction getDirection();
    protected abstract String getAmount();

    @Override
    protected String getGcode() {
        return "M120\nG91\nG1 " + getAxis() + getDirection() + getAmount().replace(",", ".") + " F360000\nG90\nM121";
    }
}
