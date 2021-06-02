package com.trolololo.workbee.jogger.operations;

public class MoveParams {
    private final AbstractMoveOperation.Axis axis;
    private final AbstractMoveOperation.Direction direction;
    private final String amount;

    MoveParams(AbstractMoveOperation.Axis axis, AbstractMoveOperation.Direction direction, String amount) {
        this.axis = axis;
        this.direction = direction;
        this.amount = amount;
    }

    public AbstractMoveOperation.Axis getAxis() {
        return axis;
    }

    public AbstractMoveOperation.Direction getDirection() {
        return direction;
    }

    public String getAmount() {
        return amount;
    }
}
