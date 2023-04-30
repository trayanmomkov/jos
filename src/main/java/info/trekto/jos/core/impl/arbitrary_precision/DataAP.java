package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.Data;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.util.Utils.deepCopy;

public class DataAP extends Data {

    public final Number[] positionX;
    public final Number[] positionY;
    public final Number[] velocityX;
    public final Number[] velocityY;
    public final Number[] accelerationX;
    public final Number[] accelerationY;
    public final Number[] mass;
    public final Number[] radius;

    public final Number[] readOnlyPositionX;
    public final Number[] readOnlyPositionY;
    public final Number[] readOnlyVelocityX;
    public final Number[] readOnlyVelocityY;
    public final Number[] readOnlyMass;
    public final Number[] readOnlyRadius;

    public DataAP(int n) {
        super(n);
        positionX = new Number[n];
        positionY = new Number[n];
        velocityX = new Number[n];
        velocityY = new Number[n];
        accelerationX = new Number[n];
        accelerationY = new Number[n];
        mass = new Number[n];
        radius = new Number[n];

        readOnlyPositionX = new Number[n];
        readOnlyPositionY = new Number[n];
        readOnlyVelocityX = new Number[n];
        readOnlyVelocityY = new Number[n];
        readOnlyMass = new Number[n];
        readOnlyRadius = new Number[n];
    }

    public void copyToReadOnly(boolean mergeOnCollision) {
        if (mergeOnCollision) {
            deepCopy(mass, readOnlyMass);
            super.copyToReadOnly();
        }
        deepCopy(positionX, readOnlyPositionX);
        deepCopy(positionY, readOnlyPositionY);
        deepCopy(radius, readOnlyRadius);
        deepCopy(velocityX, readOnlyVelocityX);
        deepCopy(velocityY, readOnlyVelocityY);
    }
}
