package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.Data;

import static info.trekto.jos.util.Utils.deepCopy;

public class DataDouble extends Data {

    public final double[] positionX;
    public final double[] positionY;
    public final double[] velocityX;
    public final double[] velocityY;
    public final double[] accelerationX;
    public final double[] accelerationY;
    public final double[] mass;
    public final double[] radius;

    public final double[] readOnlyPositionX;
    public final double[] readOnlyPositionY;
    public final double[] readOnlyVelocityX;
    public final double[] readOnlyVelocityY;
    public final double[] readOnlyMass;
    public final double[] readOnlyRadius;

    public DataDouble(int n) {
        super(n);
        positionX = new double[n];
        positionY = new double[n];
        velocityX = new double[n];
        velocityY = new double[n];
        accelerationX = new double[n];
        accelerationY = new double[n];
        mass = new double[n];
        radius = new double[n];

        readOnlyPositionX = new double[n];
        readOnlyPositionY = new double[n];
        readOnlyVelocityX = new double[n];
        readOnlyVelocityY = new double[n];
        readOnlyMass = new double[n];
        readOnlyRadius = new double[n];
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
