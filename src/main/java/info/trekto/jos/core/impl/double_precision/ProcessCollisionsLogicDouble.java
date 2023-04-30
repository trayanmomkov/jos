package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.ProcessCollisionsLogic;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.deepCopy;

public class ProcessCollisionsLogicDouble extends Kernel implements ProcessCollisionsLogic {
    private static final double TWO = 2.0;
    private static final double RATIO_FOUR_THREE = 4 / 3.0;
    private static final double PI = Math.PI;

    private final double[] positionX;
    private final double[] positionY;
    private final double[] velocityX;
    private final double[] velocityY;
    private final double[] mass;
    private final double[] radius;
    private final int[] color;
    private final boolean[] deleted;

    private final double[] readOnlyPositionX;
    private final double[] readOnlyPositionY;
    private final double[] readOnlyVelocityX;
    private final double[] readOnlyVelocityY;
    private final double[] readOnlyMass;
    private final boolean[] readOnlyDeleted;
    private final double[] readOnlyRadius;
    private final int[] readOnlyColor;

    private final int mergeOnCollision;
    private final double coefficientOfRestitution;

    private final int n;

    public ProcessCollisionsLogicDouble(int n, boolean mergeOnCollision, double coefficientOfRestitution) {
        this(new DataDouble(n), mergeOnCollision, coefficientOfRestitution);
    }

    public ProcessCollisionsLogicDouble(DataDouble data, boolean mergeOnCollision, double coefficientOfRestitution) {
        n = data.n;

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
        for (int j = 0; j < n; j++) {
            if (i != j) {
                double distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                if (distance < radius[i] + readOnlyRadius[j]) {    // If collide
                    processElasticCollision(i, j, coefficientOfRestitution);
                }
            }
        }
    }

    public void processMergeCollisions(int i) {
        if (!deleted[i]) {
            for (int j = 0; j < n; j++) {
                if (!deleted[i]) {
                    if (i != j) {
                        if (!readOnlyDeleted[j]) {
                            double distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
                            if (distance < radius[i] + readOnlyRadius[j]) {    // If collide
                                if (mass[i] > readOnlyMass[j] || mass[i] == readOnlyMass[j] && i <= j) { //  || mass[i] == readOnlyMass[j] && i < j
                                    mergeObjectsChangeBigger(j, i);
                                } else {
                                    deleted[i] = true;
                                }
                            }
                        }
                    }
                }
            }
        }
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
        final double bigMass = mass[bigger];
        final double smallMass = readOnlyMass[smaller];

        /* Decode color */
        final int biggerRed = (color[bigger] >> 16) & 0xFF;
        final int biggerGreen = (color[bigger] >> 8) & 0xFF;
        final int biggerBlue = color[bigger] & 0xFF;

        final int smallerRed = (readOnlyColor[smaller] >> 16) & 0xFF;
        final int smallerGreen = (readOnlyColor[smaller] >> 8) & 0xFF;
        final int smallerBlue = readOnlyColor[smaller] & 0xFF;

        /* Calculate new value */
        final int r = (int)round((biggerRed * bigMass + smallerRed * smallMass) / (bigMass + smallMass));
        final int g = (int)round((biggerGreen * bigMass + smallerGreen * smallMass) / (bigMass + smallMass));
        final int b = (int)round((biggerBlue * bigMass + smallerBlue * smallMass) / (bigMass + smallMass));

        /* Encode color */
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public double calculateRadiusBasedOnNewVolumeAndDensity(final int smaller, final int bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        final double smallVolume = calculateVolumeFromRadius(readOnlyRadius[smaller]);
        final double smallDensity = readOnlyMass[smaller] / smallVolume;
        final double bigVolume = calculateVolumeFromRadius(radius[bigger]);
        final double bigDensity = mass[bigger] / bigVolume;
        final double newMass = mass[bigger] + readOnlyMass[smaller];

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be,
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        final double newDensity = (smallDensity * readOnlyMass[smaller] + bigDensity * mass[bigger]) / newMass;
        final double newVolume = newMass / newDensity;

        return calculateRadiusFromVolume(newVolume);
    }

    private void changePositionOnMerging(final int smaller, final int bigger) {
        final double distanceX = positionX[bigger] - readOnlyPositionX[smaller];
        final double distanceY = positionY[bigger] - readOnlyPositionY[smaller];

        final double massRatio = readOnlyMass[smaller] / mass[bigger];

        positionX[bigger] = positionX[bigger] - distanceX * massRatio / TWO;
        positionY[bigger] = positionY[bigger] - distanceY * massRatio / TWO;
    }

    private void changeVelocityOnMerging(final int smaller, final int bigger) {
        /* We want to get already updated velocity for the current one (bigger), thus we use velocityX and not readOnlyVelocityX */
        final double totalImpulseX = readOnlyVelocityX[smaller] * readOnlyMass[smaller] + velocityX[bigger] * mass[bigger];
        final double totalImpulseY = readOnlyVelocityY[smaller] * readOnlyMass[smaller] + velocityY[bigger] * mass[bigger];
        final double totalMass = mass[bigger] + readOnlyMass[smaller];

        velocityX[bigger] = totalImpulseX / totalMass;
        velocityY[bigger] = totalImpulseY / totalMass;
    }

    public double calculateDistance(final double object1X, final double object1Y, final double object2X, final double object2Y) {
        final double x = object2X - object1X;
        final double y = object2Y - object1Y;
        return sqrt(x * x + y * y);
    }

    public double calculateVolumeFromRadius(final double radius) {
        // V = 4/3 * pi * r^3
        if (radius == 0) {
            return Double.MIN_VALUE;
        }
        return RATIO_FOUR_THREE * PI * pow(radius, 3);
    }

    public double calculateRadiusFromVolume(final double volume) {
        // V = 4/3 * pi * r^3
        if (volume == 0) {
            return Double.MIN_VALUE;
        }
        return cbrt(volume / (RATIO_FOUR_THREE * PI));
    }

    private void processElasticCollision(final int o1, final int o2, final double cor) {
        final double v1x = velocityX[o1];
        final double v1y = velocityY[o1];
        final double v2x = readOnlyVelocityX[o2];
        final double v2y = readOnlyVelocityY[o2];

        final double o1x = positionX[o1];
        final double o1y = positionY[o1];
        final double o2x = readOnlyPositionX[o2];
        final double o2y = readOnlyPositionY[o2];

        final double o1m = mass[o1];
        final double o2m = readOnlyMass[o2];

        // v'1y = v1y - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1y, o1x, o2y, o2x) * (o1y-o2y)
        // v'2x = v2x - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2x, o2y, o1x, o1y) * (o2x-o1x)
        // v'2y = v2y - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2y, o2x, o1y, o1x) * (o2y-o1y)
        // v'1x = v1x - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        velocityX[o1] = calculateVelocity(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m, cor);
        velocityY[o1] = calculateVelocity(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m, cor);

        // Here we change only o1. o2 will be changed when main iteration, getGlobalId(), pass through it.
//        velocityX[o2] = calculateVelocity(v2x, v2y, v1x, v1y, o2x, o2y, o1x, o1y, o2m, o1m, cor);
//        velocityY[o2] = calculateVelocity(v2y, v2x, v1y, v1x, o2y, o2x, o1y, o1x, o2m, o1m, cor);
    }

    private static double calculateVelocity(final double v1x, final double v1y, final double v2x, final double v2y,
                                            final double o1x, final double o1y, final double o2x, final double o2y,
                                            final double o1m, final double o2m, final double cor) {
        // v'1x = v1x - 2*o2m/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return v1x - (cor * o2m + o2m) / (o1m + o2m)
                * dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y)
                / dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y)
                * (o1x - o2x);
    }

    private static double dotProduct2D(final double ax, final double ay, final double bx, final double by, final double cx, final double cy, final double dx,
                                       final double dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax - bx) * (cx - dx) + (ay - by) * (cy - dy);
    }

    /**
     * For testing only.
     */
    @Override
    public void processElasticCollisionObjects(SimulationObject o1, SimulationObject o2, Number cor) {
        velocityX[0] = o1.getVelocity().getX().doubleValue();
        velocityY[0] = o1.getVelocity().getY().doubleValue();
        velocityX[1] = o2.getVelocity().getX().doubleValue();
        velocityY[1] = o2.getVelocity().getY().doubleValue();

        positionX[0] = o1.getX().doubleValue();
        positionY[0] = o1.getY().doubleValue();
        positionX[1] = o2.getX().doubleValue();
        positionY[1] = o2.getY().doubleValue();

        mass[0] = o1.getMass().doubleValue();
        mass[1] = o2.getMass().doubleValue();
        
        deepCopy(velocityX, readOnlyVelocityX);
        deepCopy(velocityY, readOnlyVelocityY);
        deepCopy(positionX, readOnlyPositionX);
        deepCopy(positionY, readOnlyPositionY);
        deepCopy(mass, readOnlyMass);

        processElasticCollision(0, 1, cor.doubleValue());
        processElasticCollision(1, 0, cor.doubleValue());

        o1.setVelocity(new TripleNumber(New.num(velocityX[0]), New.num(velocityY[0]), ZERO));
        o2.setVelocity(new TripleNumber(New.num(velocityX[1]), New.num(velocityY[1]), ZERO));
    }
}
