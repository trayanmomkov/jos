package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.impl.FlatBarnesHutTree;

/**
 * Like {@link MoveObjectsLogicDouble}, but with the Barnes-Hut approximation: the kernel walks the tree
 * of {@link FlatBarnesHutTree}. A group of objects which is far enough acts as one object at its center of mass.
 */
public class BarnesHutMoveObjectsLogicDouble extends Kernel {
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

    /* The Barnes-Hut tree, rebuilt on the CPU before every execution */
    private final double[] nodeCenterOfMassX;
    private final double[] nodeCenterOfMassY;
    private final double[] nodeMass;
    private final double[] nodeSideSquared;
    private final int[] nodeFirstChild;
    private final int[] nodeSkip;
    private final int[] nodeFirstBody;
    private final int[] nodeBodyCount;
    private final int[] leafBodies;

    private final double thetaSquared;
    private final double secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;
    private final double minDistance;

    private final int n;

    public BarnesHutMoveObjectsLogicDouble(DataDouble data, FlatBarnesHutTree tree, double thetaSquared,
                                           double secondsPerIteration, double minDistance, int screenWidth, int screenHeight) {
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

        nodeCenterOfMassX = tree.nodeCenterOfMassX;
        nodeCenterOfMassY = tree.nodeCenterOfMassY;
        nodeMass = tree.nodeMass;
        nodeSideSquared = tree.nodeSideSquared;
        nodeFirstChild = tree.nodeFirstChild;
        nodeSkip = tree.nodeSkip;
        nodeFirstBody = tree.nodeFirstBody;
        nodeBodyCount = tree.nodeBodyCount;
        leafBodies = tree.leafBodies;

        this.thetaSquared = thetaSquared;
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
            /* Semi-implicit (symplectic) Euler: change velocity first, then move with it (see
             * MoveObjectsLogicDouble for why moving before the velocity update injects energy). */
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
        double newAccelerationX = 0;
        double newAccelerationY = 0;
        int node = 0;
        while (node >= 0) {
            int next = nodeSkip[node];
            if (nodeFirstChild[node] >= 0) {
                /* Internal node: far enough away it acts as a single object at its center of mass. */
                double dx = nodeCenterOfMassX[node] - positionX[i];
                double dy = nodeCenterOfMassY[node] - positionY[i];
                double distanceSquared = dx * dx + dy * dy;
                /* side / distance < theta, compared with squared values to avoid sqrt */
                if (nodeSideSquared[node] < thetaSquared * distanceSquared) {
                    double distance = sqrt(distanceSquared);
                    if (distance < minDistance) {
                        distance = minDistance;
                    }
                    double force = calculateForce(mass[i], nodeMass[node], distance);
                    double forceX = force * (nodeCenterOfMassX[node] - positionX[i]) / distance;
                    double forceY = force * (nodeCenterOfMassY[node] - positionY[i]) / distance;
                    newAccelerationX = newAccelerationX + forceX / mass[i];
                    newAccelerationY = newAccelerationY + forceY / mass[i];
                } else {
                    next = nodeFirstChild[node];
                }
            } else {
                /* Leaf: exact pairwise interaction, the same as the brute force kernel. */
                int firstBody = nodeFirstBody[node];
                int bodyCount = nodeBodyCount[node];
                for (int k = 0; k < bodyCount; k++) {
                    int j = leafBodies[firstBody + k];
                    if (j != i && !readOnlyDeleted[j]) {
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
            }
            node = next;
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
