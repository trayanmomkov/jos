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

    /* Not final: the soft contact uses smaller steps. */
    private float secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;
    private final float minDistance;

    /* Soft contact (DEM), see MoveObjectsLogicDouble. Switched on by SimulationFloat. */
    private int contactEnabled = 0;
    private float contactStiffness = 0;
    private float contactDamping = 0;
    /* What run() does on the GPU: 0 - first pass of the soft contact, 1 - second pass, -1 - gravity only. */
    private int contactPhase = -1;

    private final int n;

    public MoveObjectsLogicFloat(DataFloat data, float secondsPerIteration, float minDistance, int screenWidth, int screenHeight) {
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

    /* Switches the soft contact on. The stiffness and the damping are for a unit of effective mass. */
    public void enableSoftContact(float stiffness, float damping) {
        this.contactEnabled = 1;
        this.contactStiffness = stiffness;
        this.contactDamping = damping;
    }

    public void setSecondsPerIteration(float secondsPerIteration) {
        this.secondsPerIteration = secondsPerIteration;
    }

    public void setContactPhase(int contactPhase) {
        this.contactPhase = contactPhase;
    }

    public void runOnCpu() {
        for (int i = 0; i < n; i++) {
            calculateNewValues(i);
        }
    }

    @Override
    public void run() {
        int i = getGlobalId();
        if (contactPhase == 0) {
            computeContactAcceleration(i);
        } else if (contactPhase == 1) {
            integrateContact(i);
        } else {
            calculateNewValues(i);
        }
    }

    /* Soft contact, first pass - see MoveObjectsLogicDouble. */
    public void computeContactAcceleration(int i) {
        if (!deleted[i]) {
            float newAccelerationX = 0f;
            float newAccelerationY = 0f;
            for (int j = 0; j < n; j++) {
                if (i != j && !deleted[j]) {
                    float diffX = positionX[j] - positionX[i];
                    float diffY = positionY[j] - positionY[i];
                    float actualDistance = sqrt(diffX * diffX + diffY * diffY);
                    float distance = actualDistance;
                    if (distance < minDistance) {
                        distance = minDistance;
                    }
                    float force = calculateForce(mass[i], mass[j], distance);
                    float forceX = force * diffX / distance;
                    float forceY = force * diffY / distance;
                    newAccelerationX = newAccelerationX + forceX / mass[i];
                    newAccelerationY = newAccelerationY + forceY / mass[i];

                    if (contactEnabled == 1 && actualDistance > 0) {
                        float overlap = radius[i] + radius[j] - actualDistance;
                        if (overlap > 0) {
                            float normalX = -diffX / actualDistance;
                            float normalY = -diffY / actualDistance;
                            float relativeVelocityX = velocityX[i] - velocityX[j];
                            float relativeVelocityY = velocityY[i] - velocityY[j];
                            float normalSpeed = relativeVelocityX * normalX + relativeVelocityY * normalY;
                            float contactAcceleration = (contactStiffness * overlap - contactDamping * normalSpeed)
                                    * (mass[j] / (mass[i] + mass[j]));
                            if (contactAcceleration > 0) {
                                newAccelerationX = newAccelerationX + contactAcceleration * normalX;
                                newAccelerationY = newAccelerationY + contactAcceleration * normalY;
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

    /* Copies the data to the GPU once. It stays there during all the small steps of an iteration. */
    public void uploadSoftContactState() {
        setExplicit(true);
        put(positionX).put(positionY).put(velocityX).put(velocityY);
        put(accelerationX).put(accelerationY);
        put(mass).put(radius).put(deleted);
        put(readOnlyPositionX).put(readOnlyPositionY).put(readOnlyMass).put(readOnlyDeleted);
    }

    /* Copies the new positions and velocities back from the GPU. */
    public void downloadSoftContactState() {
        get(positionX).get(positionY).get(velocityX).get(velocityY);
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
