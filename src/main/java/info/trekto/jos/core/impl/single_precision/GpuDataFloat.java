package info.trekto.jos.core.impl.single_precision;

import static info.trekto.jos.util.Utils.deepCopy;

public class GpuDataFloat extends GpuData {

    public float[] positionX;
    public float[] positionY;
    public float[] velocityX;
    public float[] velocityY;
    public float[] accelerationX;
    public float[] accelerationY;
    public float[] mass;
    public float[] radius;

    public float[] readOnlyPositionX;
    public float[] readOnlyPositionY;
    public float[] readOnlyVelocityX;
    public float[] readOnlyVelocityY;
    public float[] readOnlyMass;
    public float[] readOnlyRadius;

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
