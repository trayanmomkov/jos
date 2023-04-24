package info.trekto.jos.core.impl.single_precision;

import info.trekto.jos.core.impl.GpuData;

import static info.trekto.jos.util.Utils.deepCopy;

public class GpuDataFloat extends GpuData {

    public final float[] positionX;
    public final float[] positionY;
    public final float[] velocityX;
    public final float[] velocityY;
    public final float[] accelerationX;
    public final float[] accelerationY;
    public final float[] mass;
    public final float[] radius;

    public final float[] readOnlyPositionX;
    public final float[] readOnlyPositionY;
    public final float[] readOnlyVelocityX;
    public final float[] readOnlyVelocityY;
    public final float[] readOnlyMass;
    public final float[] readOnlyRadius;

    public GpuDataFloat(int n) {
        super(n);
        positionX = new float[n];
        positionY = new float[n];
        velocityX = new float[n];
        velocityY = new float[n];
        accelerationX = new float[n];
        accelerationY = new float[n];
        mass = new float[n];
        radius = new float[n];

        readOnlyPositionX = new float[n];
        readOnlyPositionY = new float[n];
        readOnlyVelocityX = new float[n];
        readOnlyVelocityY = new float[n];
        readOnlyMass = new float[n];
        readOnlyRadius = new float[n];
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
