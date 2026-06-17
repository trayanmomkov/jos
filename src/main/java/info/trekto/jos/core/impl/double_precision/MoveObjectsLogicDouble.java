package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;

public class MoveObjectsLogicDouble extends Kernel {
    private static final double GRAVITY = 0.000000000066743; // 6.6743×10^−11 N⋅m2/kg2

    private final double[] positionX;
    private final double[] positionY;
    private final double[] velocityX;
    private final double[] velocityY;
    private final double[] accelerationX;
    private final double[] accelerationY;
    private final double[] mass;
    private final double[] radius;
    private final boolean[] deleted;

    private final double[] readOnlyPositionX;
    private final double[] readOnlyPositionY;
    private final double[] readOnlyMass;
    private final boolean[] readOnlyDeleted;

    /* Not final: the soft contact uses smaller steps. */
    private double secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;
    private final double minDistance;

    /* Soft contact (DEM). Switched on by SimulationDouble; then there is no separate collision phase. */
    private int contactEnabled = 0;
    private double contactStiffness = 0;
    private double contactDampingFactor = 0;

    private final int n;

    public MoveObjectsLogicDouble(DataDouble data, double secondsPerIteration, double minDistance, int screenWidth, int screenHeight) {
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
        this.minDistance = minDistance;
    }

    /** Enable the soft-sphere contact force with the given auto-derived stiffness and damping factor. */
    public void enableSoftContact(double stiffness, double dampingFactor) {
        this.contactEnabled = 1;
        this.contactStiffness = stiffness;
        this.contactDampingFactor = dampingFactor;
    }

    public void setSecondsPerIteration(double secondsPerIteration) {
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

    /* Soft contact, first pass: calculates the acceleration of object i from the gravity and the contacts.
     * Nothing else is changed, so the result does not depend on the order of the threads. */
    public void computeContactAcceleration(int i) {
        if (!deleted[i]) {
            double newAccelerationX = 0;
            double newAccelerationY = 0;
            for (int j = 0; j < n; j++) {
                if (i != j && !deleted[j]) {
                    double diffX = positionX[j] - positionX[i];
                    double diffY = positionY[j] - positionY[i];
                    double actualDistance = sqrt(diffX * diffX + diffY * diffY);
                    double distance = actualDistance;
                    if (distance < minDistance) {
                        distance = minDistance;
                    }
                    double force = calculateForce(mass[i], mass[j], distance);
                    double forceX = force * diffX / distance;
                    double forceY = force * diffY / distance;
                    newAccelerationX = newAccelerationX + forceX / mass[i];
                    newAccelerationY = newAccelerationY + forceY / mass[i];

                    if (contactEnabled == 1 && actualDistance > 0) {
                        double overlap = radius[i] + radius[j] - actualDistance;
                        if (overlap > 0) {
                            double normalX = -diffX / actualDistance;   // unit normal i <- j (away from j)
                            double normalY = -diffY / actualDistance;
                            double relativeVelocityX = velocityX[i] - velocityX[j];
                            double relativeVelocityY = velocityY[i] - velocityY[j];
                            double normalSpeed = relativeVelocityX * normalX + relativeVelocityY * normalY;
                            double sumMass = mass[i] + mass[j];
                            if (sumMass > 0) {
                                double effectiveMass = mass[i] * mass[j] / sumMass;
                                double damping = contactDampingFactor * sqrt(effectiveMass * contactStiffness);
                                double contactForce = contactStiffness * overlap - damping * normalSpeed;
                                if (contactForce < 0) {
                                    contactForce = 0;   // contacts push apart, never pull together
                                }
                                newAccelerationX = newAccelerationX + contactForce * normalX / mass[i];
                                newAccelerationY = newAccelerationY + contactForce * normalY / mass[i];
                            }
                        }
                    }
                }
            }
            accelerationX[i] = newAccelerationX;
            accelerationY[i] = newAccelerationY;
        }
    }

    /* Soft contact, second pass: changes the velocity and then moves the object. */
    public void integrateContact(int i) {
        if (!deleted[i]) {
            velocityX[i] = velocityX[i] + accelerationX[i] * secondsPerIteration;
            velocityY[i] = velocityY[i] + accelerationY[i] * secondsPerIteration;
            positionX[i] = positionX[i] + velocityX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + velocityY[i] * secondsPerIteration;
            if (screenWidth != 0 && screenHeight != 0) {
                bounceFromScreenBorders(i);
            }
        }
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

            /* Change velocity first and then move with the new velocity (semi-implicit Euler).
             * Moving with the old velocity adds energy to the orbits on every iteration. */
            calculateAccelerationAndChangeVelocity(i);

            /* Move objects */
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
