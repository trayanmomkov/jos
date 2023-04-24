package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Kernel;

public class MoveObjectsLogicFloat extends Kernel {
    private static final float GRAVITY = 0.000000000066743f; // 6.6743×10^−11 N⋅m2/kg2

    private final float[] positionX;
    private final float[] positionY;
    private final float[] velocityX;
    private final float[] velocityY;
    private final float[] accelerationX;
    private final float[] accelerationY;
    private final float[] mass;
    private final float[] radius;
    private final boolean[] deleted;

    private final float[] readOnlyPositionX;
    private final float[] readOnlyPositionY;
    private final float[] readOnlyMass;
    private final boolean[] readOnlyDeleted;

    private final float secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;

    private final int n;
    
    public MoveObjectsLogicFloat(int n, float secondsPerIteration, int screenWidth, int screenHeight) {
        this(new GpuDataFloat(n), secondsPerIteration, screenWidth, screenHeight);
    }

    public MoveObjectsLogicFloat(GpuDataFloat data, float secondsPerIteration, int screenWidth, int screenHeight) {
        n = data.n;

        positionX = data.positionX;
        positionY = data.positionY;
        velocityX = data.velocityX;
        velocityY = data.velocityY;
        accelerationX = data.accelerationX;
        accelerationY = data.accelerationY;
        mass = data.mass;
        radius = data.radius;
        deleted = data.deleted;

        readOnlyPositionX = data.readOnlyPositionX;
        readOnlyPositionY = data.readOnlyPositionY;
        readOnlyMass = data.readOnlyMass;
        readOnlyDeleted = data.readOnlyDeleted;

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.secondsPerIteration = secondsPerIteration;
    }

    public void runOnCpu() {
        for (int i = 0; i < n; i++) {
            calculateNewValues(i);
        }
    }

    @Override
    public void run() {
        calculateNewValues(getGlobalId());
    }

    /**
     * !!! DO NOT CHANGE THIS METHOD, run() method and methods called from them if you don't have experience with Aparapi library!!!
     * This code is translated to OpenCL and executed on the GPU.
     * You cannot use even simple 'break' here - it is not supported by Aparapi.
     */
    public void calculateNewValues(int i) {
        if (!deleted[i]) {
            /* Speed is scalar, velocity is vector. Velocity = speed + direction. */

            /* Time T passed */

            /* Calculate acceleration */
            /* For the time T, forces accelerated the objects (changed their velocities).
             * Forces are calculated having the positions of the objects at the beginning of the period,
             * and these forces are applied for time T. */
            float newAccelerationX = 0;
            float newAccelerationY = 0;
            for (int j = 0; j < n; j++) {
                if (i != j && !readOnlyDeleted[j]) {
                    /* Calculate force */
                    float distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                    float force = calculateForce(mass[i], readOnlyMass[j], distance);
                    //       Fx = F*x/r;
                    float forceX = force * (readOnlyPositionX[j] - positionX[i]) / distance;
                    float forceY = force * (readOnlyPositionY[j] - positionY[i]) / distance;

                    /* Add to current acceleration */
                    // ax = Fx / m
                    newAccelerationX = newAccelerationX + forceX / mass[i];
                    newAccelerationY = newAccelerationY + forceY / mass[i];
                }
            }

            /* Move objects */
            /* For the time T, velocity moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            positionX[i] = positionX[i] + velocityX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + velocityY[i] * secondsPerIteration;

            /* Change velocity */
            /* For the time T, accelerations changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            velocityX[i] = velocityX[i] + accelerationX[i] * secondsPerIteration;
            velocityY[i] = velocityY[i] + accelerationY[i] * secondsPerIteration;

            /* Change the acceleration */
            accelerationX[i] = newAccelerationX;
            accelerationY[i] = newAccelerationY;

            /* Bounce from screen borders */
            if (screenWidth != 0 && screenHeight != 0) {
                bounceFromScreenBorders(i);
            }
        }
    }

    private void bounceFromScreenBorders(int i) {
        if (positionX[i] + radius[i] >= screenWidth / 2.0 || positionX[i] - radius[i] <= -screenWidth / 2.0) {
            velocityX[i] = -velocityX[i];
        }

        if (positionY[i] + radius[i] >= screenHeight / 2.0 || positionY[i] - radius[i] <= -screenHeight / 2.0) {
            velocityY[i] = -velocityY[i];
        }
    }

    public float calculateDistance(final float object1X, final float object1Y, final float object2X, final float object2Y) {
        final float x = object2X - object1X;
        final float y = object2Y - object1Y;
        return sqrt(x * x + y * y);
    }

    public static float calculateForce(final float object1Mass, final float object2Mass, final float distance) {
        return GRAVITY * object1Mass * object2Mass / (distance * distance);
    }
}
