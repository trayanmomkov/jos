package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;

public class MoveObjectsLogicDouble extends Kernel {
    private static final double GRAVITY = 0.000000000066743; // 6.6743×10^−11 N⋅m2/kg2

    private final double[] positionX;
    private final double[] positionY;
    private final double[] velocityX;
    private final double[] velocityY;
    private final double[] mass;
    private final double[] radius;
    private final boolean[] deleted;

    private final double[] readOnlyPositionX;
    private final double[] readOnlyPositionY;
    private final double[] readOnlyMass;
    private final boolean[] readOnlyDeleted;

    private final double secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;
    private final double minDistance;

    private final int n;

    public MoveObjectsLogicDouble(DataDouble data, double secondsPerIteration, double minDistance, int screenWidth, int screenHeight) {
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

            /* Move objects */
            /* For the time T, velocities have moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            positionX[i] = positionX[i] + velocityX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + velocityY[i] * secondsPerIteration;

            /* Change velocity */
            /* For the time T, accelerations have changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            calculateAccelerationAndChangeVelocity(i);

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
        double newAccelerationX = 0;
        double newAccelerationY = 0;
        for (int j = 0; j < n; j++) {
            if (i != j && !readOnlyDeleted[j]) {
                /* Calculate force */
                double distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                if (distance < minDistance) {
                    distance = minDistance;
                }
                double force = calculateForce(mass[i], readOnlyMass[j], distance);
                //       Fx = F*x/r;
                double forceX = force * (readOnlyPositionX[j] - positionX[i]) / distance;
                double forceY = force * (readOnlyPositionY[j] - positionY[i]) / distance;

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

    public double calculateDistance(final double object1X, final double object1Y, final double object2X, final double object2Y) {
        final double x = object2X - object1X;
        final double y = object2Y - object1Y;
        return sqrt(x * x + y * y);
    }

    public static double calculateForce(final double object1Mass, final double object2Mass, final double distance) {
        return GRAVITY * object1Mass * object2Mass / (distance * distance);
    }
}
