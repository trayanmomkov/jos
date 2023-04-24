package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.GpuData;

import static info.trekto.jos.util.Utils.deepCopy;

public class GpuDataDouble extends GpuData {

    public double[] positionX;
    public double[] positionY;
    public double[] velocityX;
    public double[] velocityY;
    public double[] accelerationX;
    public double[] accelerationY;
    public double[] mass;
    public double[] radius;

    public double[] readOnlyPositionX;
    public double[] readOnlyPositionY;
    public double[] readOnlyVelocityX;
    public double[] readOnlyVelocityY;
    public double[] readOnlyMass;
    public double[] readOnlyRadius;

    public GpuDataDouble(int n) {
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
