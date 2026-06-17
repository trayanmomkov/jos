package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

public class MoveObjectsLogicAP {
    private final Number gravity;

    private final Number[] positionX;
    private final Number[] positionY;
    private final Number[] velocityX;
    private final Number[] velocityY;
    private final Number[] mass;
    private final Number[] radius;
    private final boolean[] deleted;

    private final Number[] readOnlyPositionX;
    private final Number[] readOnlyPositionY;
    private final Number[] readOnlyMass;
    private final boolean[] readOnlyDeleted;

    private final Number secondsPerIteration;
    private final Number minDistance;
    private final int screenWidth;
    private final int screenHeight;

    private final int n;

    public MoveObjectsLogicAP(DataAP data, Number secondsPerIteration, Number minDistance, int screenWidth, int screenHeight) {
        gravity = New.num("0.000000000066743"); // 6.6743×10^−11 N⋅m2/kg2
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

    public void run() {
        new MoveObjectsRecursiveAction(0, n, this).compute();
    }

    public void calculateNewValues(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            calculateNewValues(i);
        }
    }

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
            positionX[i] = positionX[i].add(velocityX[i].multiply(secondsPerIteration));
            positionY[i] = positionY[i].add(velocityY[i].multiply(secondsPerIteration));

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
        Number newAccelerationX = ZERO;
        Number newAccelerationY = ZERO;
        for (int j = 0; j < n; j++) {
            if (i != j && !readOnlyDeleted[j]) {
                /* Calculate force */
                Number distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                if (distance.compareTo(minDistance) < 0) {
                    distance = minDistance;
                }
                Number force = calculateForce(mass[i], readOnlyMass[j], distance);
                //       Fx = F*x/r;
                Number forceX = force.multiply(readOnlyPositionX[j].subtract(positionX[i])).divide(distance);
                Number forceY = force.multiply(readOnlyPositionY[j].subtract(positionY[i])).divide(distance);

                /* Add to current acceleration */
                // ax = Fx / m
                newAccelerationX = newAccelerationX.add(forceX.divide(mass[i]));
                newAccelerationY = newAccelerationY.add(forceY.divide(mass[i]));
            }
        }

        velocityX[i] = velocityX[i].add(newAccelerationX.multiply(secondsPerIteration));
        velocityY[i] = velocityY[i].add(newAccelerationY.multiply(secondsPerIteration));
    }

    private void bounceFromScreenBorders(int i) {
        if (positionX[i].add(radius[i]).floatValue() >= screenWidth / 2.0 || positionX[i].subtract(radius[i]).floatValue() <= -screenWidth / 2.0) {
            velocityX[i] = velocityX[i].negate();
        }

        if (positionY[i].add(radius[i]).floatValue() >= screenHeight / 2.0 || positionY[i].subtract(radius[i]).floatValue() <= -screenHeight / 2.0) {
            velocityY[i] = velocityY[i].negate();
        }
    }

    public Number calculateDistance(final Number object1X, final Number object1Y, final Number object2X, final Number object2Y) {
        final Number x = object2X.subtract(object1X);
        final Number y = object2Y.subtract(object1Y);
        return x.multiply(x).add(y.multiply(y)).sqrt();
    }

    public Number calculateForce(final Number object1Mass, final Number object2Mass, final Number distance) {
        return gravity.multiply(object1Mass).multiply(object2Mass).divide(distance.multiply(distance));
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
