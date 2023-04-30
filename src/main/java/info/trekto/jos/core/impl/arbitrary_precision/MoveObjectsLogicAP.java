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
    private final Number[] accelerationX;
    private final Number[] accelerationY;
    private final Number[] mass;
    private final Number[] radius;
    private final boolean[] deleted;

    private final Number[] readOnlyPositionX;
    private final Number[] readOnlyPositionY;
    private final Number[] readOnlyMass;
    private final boolean[] readOnlyDeleted;

    private final Number secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;

    private final int n;

    public MoveObjectsLogicAP(DataAP data, Number secondsPerIteration, int screenWidth, int screenHeight) {
        gravity = New.num("0.000000000066743"); // 6.6743×10^−11 N⋅m2/kg2
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

    public void runOnSingleThread() {
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

            /* Time T passed */

            /* Calculate acceleration */
            /* For the time T, forces accelerated the objects (changed their velocities).
             * Forces are calculated having the positions of the objects at the beginning of the period,
             * and these forces are applied for time T. */
            Number newAccelerationX = ZERO;
            Number newAccelerationY = ZERO;
            for (int j = 0; j < n; j++) {
                if (i != j && !readOnlyDeleted[j]) {
                    /* Calculate force */
                    Number distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
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

            /* Move objects */
            /* For the time T, velocity moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            positionX[i] = positionX[i].add(velocityX[i].multiply(secondsPerIteration));
            positionY[i] = positionY[i].add(velocityY[i].multiply(secondsPerIteration));

            /* Change velocity */
            /* For the time T, accelerations changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            velocityX[i] = velocityX[i].add(accelerationX[i].multiply(secondsPerIteration));
            velocityY[i] = velocityY[i].add(accelerationY[i].multiply(secondsPerIteration));

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
