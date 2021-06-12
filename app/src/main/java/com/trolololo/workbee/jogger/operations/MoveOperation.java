package com.trolololo.workbee.jogger.operations;

import android.content.Context;

import com.trolololo.workbee.jogger.domain.Machine;
import com.trolololo.workbee.jogger.network.NetworkFragment;

public class MoveOperation extends AbstractMoveOperation {
    private final MoveParams params;

    public MoveOperation(Context context, NetworkFragment networkFragment, Machine machine, MoveParams params) {
        super(context, networkFragment, machine);
        this.params = params;
    }

    public static class MoveParamsBuilder {
        private AbstractMoveOperation.Axis axis;
        private AbstractMoveOperation.Direction direction;
        private String amount;

        public MoveParamsBuilder setAxis(AbstractMoveOperation.Axis axis) {
            this.axis = axis;
            return this;
        }

        public MoveParamsBuilder setDirection(AbstractMoveOperation.Direction direction) {
            this.direction = direction;
            return this;
        }

        public MoveParamsBuilder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public MoveParams build() {
            return new MoveParams(axis, direction, amount);
        }
    }

    @Override
    protected Axis getAxis() {
        return params.getAxis();
    }

    @Override
    protected Direction getDirection() {
        return params.getDirection();
    }

    @Override
    protected String getAmount() {
        return params.getAmount();
    }
}
