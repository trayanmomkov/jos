package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Kernel;

public class MoveObjectsLogicFloat extends Kernel {
    private static final float GRAVITY = 0.000000000066743f; // 6.6743×10^−11 N⋅m2/kg2

    private final float[] positionX;
    private final float[] positionY;
    private final float[] velocityX;
    private final float[] velocityY;
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
    private final float minDistance;

    private final int n;

    public MoveObjectsLogicFloat(DataFloat data, float secondsPerIteration, float minDistance, int screenWidth, int screenHeight) {
        n = data.n;

        positionX = data.positionX;
        positionY = data.positionY;
        velocityX = data.velocityX;
        velocityY = data.velocityY;
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
        this.minDistance = minDistance;
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

            /* Time T has passed */

            /* Semi-implicit (symplectic) Euler: update the velocity from the acceleration FIRST, then
             * move with that updated velocity. Forward Euler (moving with the old velocity before the
             * velocity update) is not symplectic and pumps energy into orbital motion, so bound
             * systems spiral apart over time. The acceleration uses the start-of-iteration positions
             * (the read-only snapshot), so the order of the two updates is the only change. */
            calculateAccelerationAndChangeVelocity(i);

            /* Move objects with the just-updated velocity. */
            positionX[i] = positionX[i] + velocityX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + velocityY[i] * secondsPerIteration;

            /* Bounce from screen borders */
            if (screenWidth != 0 && screenHeight != 0) {
                bounceFromScreenBorders(i);
            }
        }
    }

    public void calculateAccelerationAndChangeVelocity(int i) {
        /* Calculate acceleration */
        /* For the time T, forces have accelerated the objects (changed their velocities).
         * Forces are calculated having the positions of the objects at the beginning of the period. */
        float newAccelerationX = 0f;
        float newAccelerationY = 0f;
        for (int j = 0; j < n; j++) {
            if (i != j && !readOnlyDeleted[j]) {
                /* Calculate force */
                float distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                if (distance < minDistance) {
                    distance = minDistance;
                }
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

        velocityX[i] = velocityX[i] + newAccelerationX * secondsPerIteration;
        velocityY[i] = velocityY[i] + newAccelerationY * secondsPerIteration;
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
