package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.ProcessCollisionsLogic;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.deepCopy;

public class ProcessCollisionsLogicFloat extends Kernel implements ProcessCollisionsLogic {
    private static final float TWO = 2.0f;
    private static final float RATIO_FOUR_THREE = 4 / 3.0f;
    private static final float PI = (float) Math.PI;

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

    private final int mergeOnCollision;
    private final float coefficientOfRestitution;

    private final int n;

    public ProcessCollisionsLogicFloat(int n, boolean mergeOnCollision, float coefficientOfRestitution) {
        this(new GpuDataFloat(n), mergeOnCollision, coefficientOfRestitution);
    }

    public ProcessCollisionsLogicFloat(GpuDataFloat data, boolean mergeOnCollision, float coefficientOfRestitution) {
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
        for (int j = 0; j < n; j++) {
            if (i != j) {
                float distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
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
                            float distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
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

        final float massRatio = readOnlyMass[smaller] / mass[bigger];

        positionX[bigger] = positionX[bigger] - distanceX * massRatio / TWO;
        positionY[bigger] = positionY[bigger] - distanceY * massRatio / TWO;
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
        final float v1x = velocityX[o1];
        final float v1y = velocityY[o1];
        final float v2x = readOnlyVelocityX[o2];
        final float v2y = readOnlyVelocityY[o2];

        final float o1x = positionX[o1];
        final float o1y = positionY[o1];
        final float o2x = readOnlyPositionX[o2];
        final float o2y = readOnlyPositionY[o2];

        final float o1m = mass[o1];
        final float o2m = readOnlyMass[o2];

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

    private static float calculateVelocity(final float v1x, final float v1y, final float v2x, final float v2y,
                                           final float o1x, final float o1y, final float o2x, final float o2y,
                                           final float o1m, final float o2m, final float cor) {
        // v'1x = v1x - 2*o2m/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return v1x - (cor * o2m + o2m) / (o1m + o2m)
                * dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y)
                / dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y)
                * (o1x - o2x);
    }

    private static float dotProduct2D(final float ax, final float ay, final float bx, final float by, final float cx, final float cy, final float dx,
                                      final float dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax - bx) * (cx - dx) + (ay - by) * (cy - dy);
    }

    /**
     * For testing only.
     */
    @Override
    public void processElasticCollisionObjects(SimulationObject o1, SimulationObject o2, Number cor) {
        velocityX[0] = o1.getVelocity().getX().floatValue();
        velocityY[0] = o1.getVelocity().getY().floatValue();
        velocityX[1] = o2.getVelocity().getX().floatValue();
        velocityY[1] = o2.getVelocity().getY().floatValue();

        positionX[0] = o1.getX().floatValue();
        positionY[0] = o1.getY().floatValue();
        positionX[1] = o2.getX().floatValue();
        positionY[1] = o2.getY().floatValue();

        mass[0] = o1.getMass().floatValue();
        mass[1] = o2.getMass().floatValue();
        
        deepCopy(velocityX, readOnlyVelocityX);
        deepCopy(velocityY, readOnlyVelocityY);
        deepCopy(positionX, readOnlyPositionX);
        deepCopy(positionY, readOnlyPositionY);
        deepCopy(mass, readOnlyMass);

        processElasticCollision(0, 1, cor.floatValue());
        processElasticCollision(1, 0, cor.floatValue());

        o1.setVelocity(new TripleNumber(New.num(velocityX[0]), New.num(velocityY[0]), ZERO));
        o2.setVelocity(new TripleNumber(New.num(velocityX[1]), New.num(velocityY[1]), ZERO));
    }
}
