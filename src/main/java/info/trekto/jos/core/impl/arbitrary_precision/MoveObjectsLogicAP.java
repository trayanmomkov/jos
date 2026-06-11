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

    /* Barnes-Hut approximation. theta = 0: exact brute force calculation. */
    private final Number thetaSquared;
    private final boolean barnesHut;
    private BarnesHutTree tree;

    private final int n;

    public MoveObjectsLogicAP(DataAP data, Number secondsPerIteration, Number minDistance, int screenWidth, int screenHeight) {
        this(data, secondsPerIteration, minDistance, null, screenWidth, screenHeight);
    }

    public MoveObjectsLogicAP(DataAP data, Number secondsPerIteration, Number minDistance, Number barnesHutTheta,
                              int screenWidth, int screenHeight) {
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

        Number theta = barnesHutTheta == null ? ZERO : barnesHutTheta;
        this.thetaSquared = theta.multiply(theta);
        this.barnesHut = theta.compareTo(ZERO) > 0;
    }

    public void runOnCpu() {
        prepareTree();
        for (int i = 0; i < n; i++) {
            calculateNewValues(i);
        }
    }

    public void run() {
        prepareTree();
        new MoveObjectsRecursiveAction(0, n, this).compute();
    }

    /* The tree is built before the threads start and is only read by them. */
    private void prepareTree() {
        if (barnesHut) {
            tree = BarnesHutTree.build(readOnlyPositionX, readOnlyPositionY, readOnlyMass, readOnlyDeleted, n);
        }
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

            /* Change velocity first and then move with the new velocity (semi-implicit Euler).
             * Moving with the old velocity adds energy to the orbits on every iteration. */
            calculateAccelerationAndChangeVelocity(i);

            /* Move objects */
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
        Number[] newAcceleration = {ZERO, ZERO};
        if (barnesHut) {
            if (tree != null && tree.getRoot() != null) {
                addAcceleration(tree.getRoot(), i, newAcceleration);
            }
        } else {
            for (int j = 0; j < n; j++) {
                if (i != j && !readOnlyDeleted[j]) {
                    addAccelerationFromPointMass(i, readOnlyPositionX[j], readOnlyPositionY[j], readOnlyMass[j], newAcceleration);
                }
            }
        }

        velocityX[i] = velocityX[i].add(newAcceleration[0].multiply(secondsPerIteration));
        velocityY[i] = velocityY[i].add(newAcceleration[1].multiply(secondsPerIteration));
    }

    /* Adds the gravity of a tree node to the acceleration of object i. A node which is far enough
     * (side / distance < theta) acts as one object at its center of mass. Otherwise its children are visited. */
    private void addAcceleration(BarnesHutTree.Node node, int i, Number[] acceleration) {
        if (node.children != null) {
            Number dx = node.centerOfMassX.subtract(positionX[i]);
            Number dy = node.centerOfMassY.subtract(positionY[i]);
            Number distanceSquared = dx.multiply(dx).add(dy.multiply(dy));
            /* side / distance < theta, compared with squared values to avoid sqrt */
            if (node.sideSquared.compareTo(thetaSquared.multiply(distanceSquared)) < 0) {
                addAccelerationFromPointMass(i, node.centerOfMassX, node.centerOfMassY, node.mass, acceleration);
            } else {
                for (BarnesHutTree.Node child : node.children) {
                    if (child != null) {
                        addAcceleration(child, i, acceleration);
                    }
                }
            }
        } else if (node.bucket != null) {
            for (int k = 0; k < node.bucket.size(); k++) {
                int j = node.bucket.get(k);
                if (j != i) {
                    addAccelerationFromPointMass(i, readOnlyPositionX[j], readOnlyPositionY[j], readOnlyMass[j], acceleration);
                }
            }
        } else if (node.objectIndex != i) {
            int j = node.objectIndex;
            addAccelerationFromPointMass(i, readOnlyPositionX[j], readOnlyPositionY[j], readOnlyMass[j], acceleration);
        }
    }

    private void addAccelerationFromPointMass(int i, Number objectX, Number objectY, Number objectMass, Number[] acceleration) {
        /* Calculate force */
        Number distance = calculateDistance(positionX[i], positionY[i], objectX, objectY);
        if (distance.compareTo(minDistance) < 0) {
            distance = minDistance;
        }
        Number force = calculateForce(mass[i], objectMass, distance);
        //       Fx = F*x/r;
        Number forceX = force.multiply(objectX.subtract(positionX[i])).divide(distance);
        Number forceY = force.multiply(objectY.subtract(positionY[i])).divide(distance);

        /* Add to current acceleration */
        // ax = Fx / m
        acceleration[0] = acceleration[0].add(forceX.divide(mass[i]));
        acceleration[1] = acceleration[1].add(forceY.divide(mass[i]));
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
