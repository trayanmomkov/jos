package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.ProcessCollisionsLogic;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.HALF;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.IGNORED;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.PI;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.RATIO_FOUR_THREE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.deepCopy;

public class ProcessCollisionsLogicAP implements ProcessCollisionsLogic {

    private final Number[] positionX;
    private final Number[] positionY;
    private final Number[] velocityX;
    private final Number[] velocityY;
    private final Number[] mass;
    private final Number[] radius;
    private final int[] color;
    private final boolean[] deleted;

    private final Number[] readOnlyPositionX;
    private final Number[] readOnlyPositionY;
    private final Number[] readOnlyVelocityX;
    private final Number[] readOnlyVelocityY;
    private final Number[] readOnlyMass;
    private final boolean[] readOnlyDeleted;
    private final Number[] readOnlyRadius;
    private final int[] readOnlyColor;

    /* Positions at the START of the iteration (before the move phase), used only by the elastic
     * approach test, so a pair that crossed centers within one move still reads as approaching.
     * Aliased to the post-move snapshot when none is supplied, so direct callers are unchanged. */
    private final Number[] beforeMovePositionX;
    private final Number[] beforeMovePositionY;

    private final int mergeOnCollision;
    private final Number coefficientOfRestitution;

    private final int n;

    public ProcessCollisionsLogicAP(int n, boolean mergeOnCollision, Number coefficientOfRestitution) {
        this(new DataAP(n), mergeOnCollision, coefficientOfRestitution);
    }

    public ProcessCollisionsLogicAP(DataAP data, boolean mergeOnCollision, Number coefficientOfRestitution) {
        this(data, mergeOnCollision, coefficientOfRestitution, data.readOnlyPositionX, data.readOnlyPositionY);
    }

    public ProcessCollisionsLogicAP(DataAP data, boolean mergeOnCollision, Number coefficientOfRestitution,
                                    Number[] beforeMovePositionX, Number[] beforeMovePositionY) {
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

        this.mergeOnCollision = mergeOnCollision ? 1 : 0;
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    public void runOnSingleThread() {
        for (int i = 0; i < n; i++) {
            if (mergeOnCollision == 1) {
                processMergeCollisions(i);
            } else {
                processElasticCollisions(i);
            }
        }
    }

    public void run() {
        new CheckCollisionsRecursiveAction(0, n, this).compute();
    }

    public void processCollisions(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (mergeOnCollision == 1) {
                processMergeCollisions(i);
            } else {
                processElasticCollisions(i);
            }
        }
    }

    public void processElasticCollisions(int i) {
        for (int j = 0; j < n; j++) {
            if (i != j) {
                Number distance = calculateDistance(readOnlyPositionX[i], readOnlyPositionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                if (distance.compareTo(readOnlyRadius[i].add(readOnlyRadius[j])) < 0) {    // If collide
                    processElasticCollision(i, j, coefficientOfRestitution);
                    pushOutOfOverlap(i, j, distance);
                }
            }
        }
    }

    /**
     * Solid objects cannot interpenetrate, so an overlap is resolved by moving the objects apart.
     * Each object of the pair moves out by its inverse-mass share of the overlap (the pair's center
     * of mass stays in place), a HALF (the de-penetration rate) per iteration to avoid sudden jumps.
     * Only positions change; the velocities and so the kinetic energy are not affected.
     */
    private void pushOutOfOverlap(final int i, final int j, final Number distance) {
        if (distance.compareTo(ZERO) > 0) {
            final Number overlap = readOnlyRadius[i].add(readOnlyRadius[j]).subtract(distance);
            final Number push = overlap.multiply(readOnlyMass[j].divide(readOnlyMass[i].add(readOnlyMass[j]))).multiply(HALF);
            positionX[i] = positionX[i].add(push.multiply(readOnlyPositionX[i].subtract(readOnlyPositionX[j])).divide(distance));
            positionY[i] = positionY[i].add(push.multiply(readOnlyPositionY[i].subtract(readOnlyPositionY[j])).divide(distance));
        }
    }

    public void processMergeCollisions(int i) {
        if (!readOnlyDeleted[i]) {
            /* All decisions are made from the read-only snapshots, so every thread reaches the
             * same conclusions, and the mass of an absorbed object is added exactly once. */
            int winnerOverMe = findWinnerOver(i);
            if (winnerOverMe >= 0) {
                /* This object is being absorbed, so it must not absorb anything itself - the winner
                 * reads this object's mass from the snapshot, and everything eaten now would be lost.
                 * It is deleted only if its winner survives; otherwise it lives on and the chain of
                 * overlapping objects is resolved in the next iterations. */
                if (findWinnerOver(winnerOverMe) < 0) {
                    deleted[i] = true;
                }
            } else {
                for (int j = 0; j < n; j++) {
                    if (i != j && !readOnlyDeleted[j] && overlaps(i, j) == 1 && isBigger(i, j) == 1
                            && findWinnerOver(j) == i) {
                        mergeObjectsChangeBigger(j, i);
                    }
                }
            }
        }
    }

    /**
     * The winner over an object is the single object which absorbs it on merging: the most massive
     * (with the smaller index on equal masses) of the alive objects which overlap it and are bigger
     * than it. Returns -1 if there is no such object.
     */
    private int findWinnerOver(int x) {
        int winner = -1;
        for (int l = 0; l < n; l++) {
            if (l != x && !readOnlyDeleted[l] && overlaps(l, x) == 1 && isBigger(l, x) == 1
                    && (winner < 0 || isBigger(l, winner) == 1)) {
                winner = l;
            }
        }
        return winner;
    }

    /**
     * Strict deterministic order: by mass, the smaller index wins on equal masses.
     * Returns 1/0 instead of boolean to mirror the GPU implementations.
     */
    private int isBigger(int a, int b) {
        int result = 0;
        if (readOnlyMass[a].compareTo(readOnlyMass[b]) > 0
                || readOnlyMass[a].compareTo(readOnlyMass[b]) == 0 && a < b) {
            result = 1;
        }
        return result;
    }

    private int overlaps(int a, int b) {
        int result = 0;
        Number distance = calculateDistance(readOnlyPositionX[a], readOnlyPositionY[a], readOnlyPositionX[b], readOnlyPositionY[b]);
        if (distance.compareTo(readOnlyRadius[a].add(readOnlyRadius[b])) < 0) {
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
        mass[bigger] = mass[bigger].add(readOnlyMass[smaller]);
    }

    private int calculateColor(final int smaller, final int bigger) {
        final float bigMass = mass[bigger].floatValue();
        final float smallMass = readOnlyMass[smaller].floatValue();

        /* Decode color */
        final int biggerRed = (color[bigger] >> 16) & 0xFF;
        final int biggerGreen = (color[bigger] >> 8) & 0xFF;
        final int biggerBlue = color[bigger] & 0xFF;

        final int smallerRed = (readOnlyColor[smaller] >> 16) & 0xFF;
        final int smallerGreen = (readOnlyColor[smaller] >> 8) & 0xFF;
        final int smallerBlue = readOnlyColor[smaller] & 0xFF;

        /* Calculate new value */
        final int r = Math.round((biggerRed * bigMass + smallerRed * smallMass) / (bigMass + smallMass));
        final int g = Math.round((biggerGreen * bigMass + smallerGreen * smallMass) / (bigMass + smallMass));
        final int b = Math.round((biggerBlue * bigMass + smallerBlue * smallMass) / (bigMass + smallMass));

        /* Encode color */
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public Number calculateRadiusBasedOnNewVolumeAndDensity(final int smaller, final int bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        final Number smallVolume = calculateVolumeFromRadius(readOnlyRadius[smaller]);
        final Number smallDensity = readOnlyMass[smaller].divide(smallVolume);
        final Number bigVolume = calculateVolumeFromRadius(radius[bigger]);
        final Number bigDensity = mass[bigger].divide(bigVolume);
        final Number newMass = mass[bigger].add(readOnlyMass[smaller]);

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be,
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        final Number newDensity = smallDensity.multiply(readOnlyMass[smaller]).add(bigDensity.multiply(mass[bigger])).divide(newMass);
        final Number newVolume = newMass.divide(newDensity);

        return calculateRadiusFromVolume(newVolume);
    }

    private void changePositionOnMerging(final int smaller, final int bigger) {
        final Number distanceX = positionX[bigger].subtract(readOnlyPositionX[smaller]);
        final Number distanceY = positionY[bigger].subtract(readOnlyPositionY[smaller]);

        /* Place the merged object at the common center of mass. mass[bigger] does not
         * include the mass of the smaller object yet - it is added at the end of the merge. */
        final Number massRatio = readOnlyMass[smaller].divide(mass[bigger].add(readOnlyMass[smaller]));

        positionX[bigger] = positionX[bigger].subtract(distanceX.multiply(massRatio));
        positionY[bigger] = positionY[bigger].subtract(distanceY.multiply(massRatio));
    }

    private void changeVelocityOnMerging(final int smaller, final int bigger) {
        /* We want to get already updated velocity for the current one (bigger), thus we use velocityX and not readOnlyVelocityX */
        final Number totalImpulseX = readOnlyVelocityX[smaller].multiply(readOnlyMass[smaller]).add(velocityX[bigger].multiply(mass[bigger]));
        final Number totalImpulseY = readOnlyVelocityY[smaller].multiply(readOnlyMass[smaller]).add(velocityY[bigger].multiply(mass[bigger]));
        final Number totalMass = mass[bigger].add(readOnlyMass[smaller]);

        velocityX[bigger] = totalImpulseX.divide(totalMass);
        velocityY[bigger] = totalImpulseY.divide(totalMass);
    }

    public Number calculateDistance(final Number object1X, final Number object1Y, final Number object2X, final Number object2Y) {
        final Number x = object2X.subtract(object1X);
        final Number y = object2Y.subtract(object1Y);
        return x.multiply(x).add(y.multiply(y)).sqrt();
    }

    public static Number calculateVolumeFromRadius(final Number radius) {
        // V = 4/3 * pi * r^3
        return RATIO_FOUR_THREE.multiply(PI).multiply(radius.pow(3));
    }

    public static Number calculateRadiusFromVolume(final Number volume) {
        // V = 4/3 * pi * r^3
        return IGNORED.cbrt(volume.divide(RATIO_FOUR_THREE.multiply(PI)));
    }

    private void processElasticCollision(final int o1, final int o2, final Number cor) {
        /* All inputs are read from the read-only snapshots. The two threads of a colliding pair
         * then compute exactly opposite impulses, and the velocity deltas of simultaneous
         * collisions with several objects add up independently of the processing order,
         * so the total momentum is conserved exactly. */
        final Number v1x = readOnlyVelocityX[o1];
        final Number v1y = readOnlyVelocityY[o1];
        final Number v2x = readOnlyVelocityX[o2];
        final Number v2y = readOnlyVelocityY[o2];

        final Number o1x = readOnlyPositionX[o1];
        final Number o1y = readOnlyPositionY[o1];
        final Number o2x = readOnlyPositionX[o2];
        final Number o2y = readOnlyPositionY[o2];

        final Number o1m = readOnlyMass[o1];
        final Number o2m = readOnlyMass[o2];

        /* The approach test uses the positions from the start of the iteration (before the move),
         * so a pair that crossed centers within one move still reads as approaching and bounces. */
        final Number b1x = beforeMovePositionX[o1];
        final Number b1y = beforeMovePositionY[o1];
        final Number b2x = beforeMovePositionX[o2];
        final Number b2y = beforeMovePositionY[o2];

        /* Apply the impulse only if the objects are moving towards each other: <v1 - v2, x1 - x2> < 0.
         * An overlapping pair which is already separating (it bounced in a previous iteration, but the
         * overlap is not yet cleared) must be left alone. Otherwise the impulse is applied with an
         * opposite sign, pushes the objects back into each other and they remain stuck together,
         * losing velocity on every iteration. */
        if (dotProduct2D(v1x, v1y, v2x, v2y, b1x, b1y, b2x, b2y).compareTo(ZERO) < 0) {
            velocityX[o1] = velocityX[o1].add(calculateVelocityDelta(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m, cor));
            velocityY[o1] = velocityY[o1].add(calculateVelocityDelta(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m, cor));
        }

        // Here we change only o1. o2 will be changed when main iteration, getGlobalId(), pass through it.
    }

    private Number calculateVelocityDelta(final Number v1x, final Number v1y, final Number v2x, final Number v2y,
                                          final Number o1x, final Number o1y, final Number o2x, final Number o2y,
                                          final Number o1m, final Number o2m, final Number cor) {
        // delta v1x = -(cor*o2m + o2m)/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return (cor.multiply(o2m).add(o2m))
                .divide(o1m.add(o2m))
                .multiply(dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y))
                .divide(dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y))
                .multiply(o1x.subtract(o2x))
                .negate();
    }

    private Number dotProduct2D(final Number ax, final Number ay, final Number bx, final Number by, final Number cx, final Number cy,
                                final Number dx, final Number dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax.subtract(bx)).multiply(cx.subtract(dx)).add(ay.subtract(by).multiply(cy.subtract(dy)));
    }

    /**
     * For testing only.
     */
    @Override
    public void processElasticCollisionObjects(SimulationObject o1, SimulationObject o2, Number cor) {
        velocityX[0] = o1.getVelocity().getX();
        velocityY[0] = o1.getVelocity().getY();
        velocityX[1] = o2.getVelocity().getX();
        velocityY[1] = o2.getVelocity().getY();

        positionX[0] = o1.getX();
        positionY[0] = o1.getY();
        positionX[1] = o2.getX();
        positionY[1] = o2.getY();

        mass[0] = o1.getMass();
        mass[1] = o2.getMass();
        
        deepCopy(velocityX, readOnlyVelocityX);
        deepCopy(velocityY, readOnlyVelocityY);
        deepCopy(positionX, readOnlyPositionX);
        deepCopy(positionY, readOnlyPositionY);
        deepCopy(mass, readOnlyMass);

        processElasticCollision(0, 1, cor);
        processElasticCollision(1, 0, cor);

        o1.setVelocity(new TripleNumber(velocityX[0], velocityY[0], ZERO));
        o2.setVelocity(new TripleNumber(velocityX[1], velocityY[1], ZERO));
    }
}
