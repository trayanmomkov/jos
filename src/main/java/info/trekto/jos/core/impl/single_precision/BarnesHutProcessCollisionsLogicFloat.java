package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.impl.FlatBarnesHutTree;

/**
 * Like {@link ProcessCollisionsLogicFloat}, but the overlapping objects are found with the Barnes-Hut tree
 * instead of checking all pairs. A node is skipped when the object cannot reach any object in it.
 */
public class BarnesHutProcessCollisionsLogicFloat extends Kernel {
    private static final float RATIO_FOUR_THREE = 4 / 3.0f;
    private static final float PI = (float) Math.PI;
    /* The part of the overlap between two objects which is resolved per iteration. */
    private static final float DEPENETRATION_RATE = 0.5f;

    private final float[] positionX;
    private final float[] positionY;
    private final float[] velocityX;
    private final float[] velocityY;
    private final float[] mass;
    private final float[] radius;
    private final int[] color;
    private final boolean[] deleted;

    private final float[] readOnlyPositionX;
    private final float[] readOnlyPositionY;
    private final float[] readOnlyVelocityX;
    private final float[] readOnlyVelocityY;
    private final float[] readOnlyMass;
    private final boolean[] readOnlyDeleted;
    private final float[] readOnlyRadius;
    private final int[] readOnlyColor;

    /* The Barnes-Hut tree, rebuilt on the CPU before every execution */
    private final float[] nodeMinX;
    private final float[] nodeMinY;
    private final float[] nodeSide;
    private final float[] nodeMaxRadius;
    private final int[] nodeFirstChild;
    private final int[] nodeSkip;
    private final int[] nodeFirstBody;
    private final int[] nodeBodyCount;
    private final int[] leafBodies;

    private final int mergeOnCollision;
    private final float coefficientOfRestitution;

    /* The tree was built before the move. The search is widened by the slack - the biggest distance
     * an object has moved since then - so no overlap is missed. */
    private float slack;

    /* Positions at the START of the iteration (before the move), used only by the elastic approach
     * test - see ProcessCollisionsLogicDouble. Aliased to the post-move snapshot when none is given. */
    private final float[] beforeMovePositionX;
    private final float[] beforeMovePositionY;

    private final int n;

    public BarnesHutProcessCollisionsLogicFloat(DataFloat data, FlatBarnesHutTree tree, FloatBarnesHutTreeArrays treeArrays,
                                                boolean mergeOnCollision, float coefficientOfRestitution) {
        this(data, tree, treeArrays, mergeOnCollision, coefficientOfRestitution, data.readOnlyPositionX, data.readOnlyPositionY);
    }

    public BarnesHutProcessCollisionsLogicFloat(DataFloat data, FlatBarnesHutTree tree, FloatBarnesHutTreeArrays treeArrays,
                                                boolean mergeOnCollision, float coefficientOfRestitution,
                                                float[] beforeMovePositionX, float[] beforeMovePositionY) {
        n = data.n;
        this.beforeMovePositionX = beforeMovePositionX;
        this.beforeMovePositionY = beforeMovePositionY;

        positionX = data.positionX;
        positionY = data.positionY;
        velocityX = data.velocityX;
        velocityY = data.velocityY;
        mass = data.mass;
        radius = data.radius;
        color = data.color;
        deleted = data.deleted;

        readOnlyPositionX = data.readOnlyPositionX;
        readOnlyPositionY = data.readOnlyPositionY;
        readOnlyVelocityX = data.readOnlyVelocityX;
        readOnlyVelocityY = data.readOnlyVelocityY;
        readOnlyMass = data.readOnlyMass;
        readOnlyDeleted = data.readOnlyDeleted;
        readOnlyRadius = data.readOnlyRadius;
        readOnlyColor = data.readOnlyColor;

        nodeMinX = treeArrays.nodeMinX;
        nodeMinY = treeArrays.nodeMinY;
        nodeSide = treeArrays.nodeSide;
        nodeMaxRadius = treeArrays.nodeMaxRadius;
        nodeFirstChild = tree.nodeFirstChild;
        nodeSkip = tree.nodeSkip;
        nodeFirstBody = tree.nodeFirstBody;
        nodeBodyCount = tree.nodeBodyCount;
        leafBodies = tree.leafBodies;

        this.mergeOnCollision = mergeOnCollision ? 1 : 0;
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    public void setSlack(float slack) {
        this.slack = slack;
    }

    public void runOnCpu() {
        for (int i = 0; i < n; i++) {
            if (mergeOnCollision == 1) {
                processMergeCollisions(i);
            } else {
                processElasticCollisions(i);
            }
        }
    }

    /**
     * !!! DO NOT CHANGE THIS METHOD and methods called from it if you don't have experience with Aparapi library!!!
     * This code is translated to OpenCL and executed on the GPU.
     * You cannot use even simple 'break' here - it is not supported by Aparapi.
     */
    @Override
    public void run() {
        if (mergeOnCollision == 1) {
            processMergeCollisions(getGlobalId());
        } else {
            processElasticCollisions(getGlobalId());
        }
    }

    public void processElasticCollisions(int i) {
        int node = 0;
        while (node >= 0) {
            int next = nodeSkip[node];
            float reach = readOnlyRadius[i] + nodeMaxRadius[node] + slack;
            if (minDistanceSquaredToNode(node, readOnlyPositionX[i], readOnlyPositionY[i]) <= reach * reach) {
                if (nodeFirstChild[node] >= 0) {
                    next = nodeFirstChild[node];
                } else {
                    int firstBody = nodeFirstBody[node];
                    int bodyCount = nodeBodyCount[node];
                    for (int k = 0; k < bodyCount; k++) {
                        int j = leafBodies[firstBody + k];
                        if (i != j) {
                            float distance = calculateDistance(readOnlyPositionX[i], readOnlyPositionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                            if (distance < readOnlyRadius[i] + readOnlyRadius[j]) {    // If collide
                                processElasticCollision(i, j, coefficientOfRestitution);
                                pushOutOfOverlap(i, j, distance);
                            }
                        }
                    }
                }
            }
            node = next;
        }
    }

    public void processMergeCollisions(int i) {
        /* Declared at method level: Aparapi loses declarations of locals introduced in nested
         * blocks when generating OpenCL, and the kernel fails to compile. */
        int node = 0;
        if (!readOnlyDeleted[i]) {
            /* All decisions are made from the read-only snapshots, so every thread reaches the
             * same conclusions, and the mass of an absorbed object is added exactly once. */
            int winnerOverMe = findWinnerOver(i);
            if (winnerOverMe >= 0) {
                /* An object which is being absorbed must not absorb others - their mass would be lost.
                 * It is deleted only if its winner survives this iteration. */
                if (findWinnerOver(winnerOverMe) < 0) {
                    deleted[i] = true;
                }
            } else {
                while (node >= 0) {
                    int next = nodeSkip[node];
                    float reach = readOnlyRadius[i] + nodeMaxRadius[node] + slack;
                    if (minDistanceSquaredToNode(node, readOnlyPositionX[i], readOnlyPositionY[i]) <= reach * reach) {
                        if (nodeFirstChild[node] >= 0) {
                            next = nodeFirstChild[node];
                        } else {
                            int firstBody = nodeFirstBody[node];
                            int bodyCount = nodeBodyCount[node];
                            for (int k = 0; k < bodyCount; k++) {
                                int j = leafBodies[firstBody + k];
                                if (i != j && !readOnlyDeleted[j] && overlaps(i, j) == 1 && isBigger(i, j) == 1
                                        && findWinnerOver(j) == i) {
                                    mergeObjectsChangeBigger(j, i);
                                }
                            }
                        }
                    }
                    node = next;
                }
            }
        }
    }

    /* The object which absorbs x on merging: the biggest of the alive objects
     * which overlap x and are bigger than it. -1 if there is no such object. */
    private int findWinnerOver(int x) {
        int winner = -1;
        int node = 0;
        while (node >= 0) {
            int next = nodeSkip[node];
            float reach = readOnlyRadius[x] + nodeMaxRadius[node] + slack;
            if (minDistanceSquaredToNode(node, readOnlyPositionX[x], readOnlyPositionY[x]) <= reach * reach) {
                if (nodeFirstChild[node] >= 0) {
                    next = nodeFirstChild[node];
                } else {
                    int firstBody = nodeFirstBody[node];
                    int bodyCount = nodeBodyCount[node];
                    for (int k = 0; k < bodyCount; k++) {
                        int l = leafBodies[firstBody + k];
                        if (l != x && !readOnlyDeleted[l] && overlaps(l, x) == 1 && isBigger(l, x) == 1
                                && (winner < 0 || isBigger(l, winner) == 1)) {
                            winner = l;
                        }
                    }
                }
            }
            node = next;
        }
        return winner;
    }

    /**
     * The smallest possible squared distance between a point and the node square.
     * Zero when the point is inside the square.
     */
    private float minDistanceSquaredToNode(int node, float px, float py) {
        float clampedX = px;
        if (clampedX > nodeMinX[node] + nodeSide[node]) {
            clampedX = nodeMinX[node] + nodeSide[node];
        }
        if (clampedX < nodeMinX[node]) {
            clampedX = nodeMinX[node];
        }
        float clampedY = py;
        if (clampedY > nodeMinY[node] + nodeSide[node]) {
            clampedY = nodeMinY[node] + nodeSide[node];
        }
        if (clampedY < nodeMinY[node]) {
            clampedY = nodeMinY[node];
        }
        float dx = clampedX - px;
        float dy = clampedY - py;
        return dx * dx + dy * dy;
    }

    /* By mass. On equal masses the smaller index wins. Returns 1/0 instead of boolean, for Aparapi. */
    private int isBigger(int a, int b) {
        int result = 0;
        if (readOnlyMass[a] > readOnlyMass[b] || (readOnlyMass[a] == readOnlyMass[b] && a < b)) {
            result = 1;
        }
        return result;
    }

    private int overlaps(int a, int b) {
        int result = 0;
        float distance = calculateDistance(readOnlyPositionX[a], readOnlyPositionY[a], readOnlyPositionX[b], readOnlyPositionY[b]);
        if (distance < readOnlyRadius[a] + readOnlyRadius[b]) {
            result = 1;
        }
        return result;
    }

    private void mergeObjectsChangeBigger(int smaller, int bigger) {
        /* Velocity */
        changeVelocityOnMerging(smaller, bigger);

        /* Position */
        changePositionOnMerging(smaller, bigger);

        /* Color */
        color[bigger] = calculateColor(smaller, bigger);

        /* Volume (radius) */
        radius[bigger] = calculateRadiusBasedOnNewVolumeAndDensity(smaller, bigger);

        /* Mass */
        mass[bigger] = mass[bigger] + readOnlyMass[smaller];
    }

    private int calculateColor(final int smaller, final int bigger) {
        final float bigMass = mass[bigger];
        final float smallMass = readOnlyMass[smaller];

        /* Decode color */
        final int biggerRed = (color[bigger] >> 16) & 0xFF;
        final int biggerGreen = (color[bigger] >> 8) & 0xFF;
        final int biggerBlue = color[bigger] & 0xFF;

        final int smallerRed = (readOnlyColor[smaller] >> 16) & 0xFF;
        final int smallerGreen = (readOnlyColor[smaller] >> 8) & 0xFF;
        final int smallerBlue = readOnlyColor[smaller] & 0xFF;

        /* Calculate new value */
        final int r = round((biggerRed * bigMass + smallerRed * smallMass) / (bigMass + smallMass));
        final int g = round((biggerGreen * bigMass + smallerGreen * smallMass) / (bigMass + smallMass));
        final int b = round((biggerBlue * bigMass + smallerBlue * smallMass) / (bigMass + smallMass));

        /* Encode color */
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public float calculateRadiusBasedOnNewVolumeAndDensity(final int smaller, final int bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        final float smallVolume = calculateVolumeFromRadius(readOnlyRadius[smaller]);
        final float smallDensity = readOnlyMass[smaller] / smallVolume;
        final float bigVolume = calculateVolumeFromRadius(radius[bigger]);
        final float bigDensity = mass[bigger] / bigVolume;
        final float newMass = mass[bigger] + readOnlyMass[smaller];

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be,
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        final float newDensity = (smallDensity * readOnlyMass[smaller] + bigDensity * mass[bigger]) / newMass;
        final float newVolume = newMass / newDensity;

        return calculateRadiusFromVolume(newVolume);
    }

    private void changePositionOnMerging(final int smaller, final int bigger) {
        final float distanceX = positionX[bigger] - readOnlyPositionX[smaller];
        final float distanceY = positionY[bigger] - readOnlyPositionY[smaller];

        /* Place the merged object at the common center of mass. mass[bigger] does not
         * include the mass of the smaller object yet - it is added at the end of the merge. */
        final float massRatio = readOnlyMass[smaller] / (mass[bigger] + readOnlyMass[smaller]);

        positionX[bigger] = positionX[bigger] - distanceX * massRatio;
        positionY[bigger] = positionY[bigger] - distanceY * massRatio;
    }

    private void changeVelocityOnMerging(final int smaller, final int bigger) {
        /* We want to get already updated velocity for the current one (bigger), thus we use velocityX and not readOnlyVelocityX */
        final float totalImpulseX = readOnlyVelocityX[smaller] * readOnlyMass[smaller] + velocityX[bigger] * mass[bigger];
        final float totalImpulseY = readOnlyVelocityY[smaller] * readOnlyMass[smaller] + velocityY[bigger] * mass[bigger];
        final float totalMass = mass[bigger] + readOnlyMass[smaller];

        velocityX[bigger] = totalImpulseX / totalMass;
        velocityY[bigger] = totalImpulseY / totalMass;
    }

    public float calculateDistance(final float object1X, final float object1Y, final float object2X, final float object2Y) {
        final float x = object2X - object1X;
        final float y = object2Y - object1Y;
        return sqrt(x * x + y * y);
    }

    public float calculateVolumeFromRadius(final float radius) {
        // V = 4/3 * pi * r^3
        if (radius == 0) {
            return Float.MIN_VALUE;
        }
        return RATIO_FOUR_THREE * PI * pow(radius, 3);
    }

    public float calculateRadiusFromVolume(final float volume) {
        // V = 4/3 * pi * r^3
        if (volume == 0) {
            return Float.MIN_VALUE;
        }
        return cbrt(volume / (RATIO_FOUR_THREE * PI));
    }

    private void processElasticCollision(final int o1, final int o2, final float cor) {
        /* Only the read-only snapshots are used. This way both objects of a pair calculate
         * opposite impulses and the total momentum is conserved. */
        final float v1x = readOnlyVelocityX[o1];
        final float v1y = readOnlyVelocityY[o1];
        final float v2x = readOnlyVelocityX[o2];
        final float v2y = readOnlyVelocityY[o2];

        final float o1x = readOnlyPositionX[o1];
        final float o1y = readOnlyPositionY[o1];
        final float o2x = readOnlyPositionX[o2];
        final float o2y = readOnlyPositionY[o2];

        final float o1m = readOnlyMass[o1];
        final float o2m = readOnlyMass[o2];

        final float b1x = beforeMovePositionX[o1];
        final float b1y = beforeMovePositionY[o1];
        final float b2x = beforeMovePositionX[o2];
        final float b2y = beforeMovePositionY[o2];

        /* Collide only if the objects approach each other: <v1 - v2, x1 - x2> < 0.
         * Otherwise a pair which has already bounced, but still overlaps, sticks together. */
        if (dotProduct2D(v1x, v1y, v2x, v2y, b1x, b1y, b2x, b2y) < 0) {
            velocityX[o1] = velocityX[o1] + calculateVelocityDelta(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m, cor);
            velocityY[o1] = velocityY[o1] + calculateVelocityDelta(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m, cor);
        }

        // Here we change only o1. o2 will be changed when main iteration, getGlobalId(), pass through it.
    }

    /* Moves the overlapping objects apart, keeping their center of mass in place.
     * Only a part of the overlap is resolved per iteration, to avoid sudden jumps. */
    private void pushOutOfOverlap(final int i, final int j, final float distance) {
        if (distance > 0) {
            final float overlap = readOnlyRadius[i] + readOnlyRadius[j] - distance;
            final float push = overlap * (readOnlyMass[j] / (readOnlyMass[i] + readOnlyMass[j])) * DEPENETRATION_RATE;
            positionX[i] = positionX[i] + push * (readOnlyPositionX[i] - readOnlyPositionX[j]) / distance;
            positionY[i] = positionY[i] + push * (readOnlyPositionY[i] - readOnlyPositionY[j]) / distance;
        }
    }

    private static float calculateVelocityDelta(final float v1x, final float v1y, final float v2x, final float v2y,
                                                final float o1x, final float o1y, final float o2x, final float o2y,
                                                final float o1m, final float o2m, final float cor) {
        // delta v1x = -(cor*o2m + o2m)/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return -((cor * o2m + o2m) / (o1m + o2m)
                * dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y)
                / dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y)
                * (o1x - o2x));
    }

    private static float dotProduct2D(final float ax, final float ay, final float bx, final float by, final float cx, final float cy, final float dx,
                                      final float dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax - bx) * (cx - dx) + (ay - by) * (cy - dy);
    }
}
