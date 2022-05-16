package info.trekto.jos.core.impl.single_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

public class SimulationLogicFloat extends Kernel implements SimulationLogic {
    private static final float TWO = 2.0f;
    private static final float RATIO_FOUR_THREE = 4 / 3.0f;
    private static final float GRAVITY = 0.000000000066743f; // 6.6743×10^−11 N⋅m2/kg2
    private static final float PI = (float)Math.PI;

    public final float[] positionX;
    public final float[] positionY;
    public final float[] speedX;
    public final float[] speedY;
    public final float[] accelerationX;
    public final float[] accelerationY;
    public final float[] mass;
    public final float[] radius;
    public final String[] id;
    public final int[] color;
    public final boolean[] deleted;

    public final float[] readOnlyPositionX;
    public final float[] readOnlyPositionY;
    public final float[] readOnlySpeedX;
    public final float[] readOnlySpeedY;
    public final float[] readOnlyAccelerationX;
    public final float[] readOnlyAccelerationY;
    public final float[] readOnlyMass;
    public final float[] readOnlyRadius;
    public final int[] readOnlyColor;
    public final boolean[] readOnlyDeleted;

    private final float secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;

    public SimulationLogicFloat(int numberOfObjects, float secondsPerIteration, int screenWidth, int screenHeight) {
        int n = numberOfObjects;
        this.secondsPerIteration = secondsPerIteration;

        positionX = new float[n];
        positionY = new float[n];
        speedX = new float[n];
        speedY = new float[n];
        accelerationX = new float[n];
        accelerationY = new float[n];
        mass = new float[n];
        radius = new float[n];
        id = new String[n];
        color = new int[n];
        deleted = new boolean[n];

        readOnlyPositionX = new float[n];
        readOnlyPositionY = new float[n];
        readOnlySpeedX = new float[n];
        readOnlySpeedY = new float[n];
        readOnlyAccelerationX = new float[n];
        readOnlyAccelerationY = new float[n];
        readOnlyMass = new float[n];
        readOnlyRadius = new float[n];
        readOnlyColor = new int[n];
        readOnlyDeleted = new boolean[n];
        
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
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
            /* Speed is scalar, velocity is vector. Velocity = speed + direction. */

            /* Time T passed */

            /* Calculate acceleration */
            /* For the time T, forces accelerated the objects (changed their velocities).
             * Forces are calculated having the positions of the objects at the beginning of the period,
             * and these forces are applied for time T. */
            float newAccelerationX = 0;
            float newAccelerationY = 0;
            for (int j = 0; j < readOnlyPositionX.length; j++) {
                if (i != j && !readOnlyDeleted[j]) {
                    /* Calculate force */
                    float distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
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

            /* Move objects */
            /* For the time T, velocity moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            positionX[i] = positionX[i] + speedX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + speedY[i] * secondsPerIteration;

            /* Change speed */
            /* For the time T, accelerations changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            speedX[i] = speedX[i] + accelerationX[i] * secondsPerIteration;
            speedY[i] = speedY[i] + accelerationY[i] * secondsPerIteration;
            
            /* Change the acceleration */
            accelerationX[i] = newAccelerationX;
            accelerationY[i] = newAccelerationY;
            
            /* Bounce from walls */
            if (screenWidth != 0 && screenHeight != 0) {
                bounceFromWalls(i);
            }
        }
    }

    private void bounceFromWalls(int i) {
        if (positionX[i] + radius[i] >= screenWidth / 2.0 || positionX[i] - radius[i] <= -screenWidth / 2.0) {
            speedX[i] = -speedX[i];
        }

        if (positionY[i] + radius[i] >= screenHeight / 2.0 || positionY[i] - radius[i] <= -screenHeight / 2.0) {
            speedY[i] = -speedY[i];
        }
    }

    public void processCollisions() {
        boolean elasticity = false;
        Set<Map.Entry<Integer, Integer>> processedElasticCollision = null;
        if (elasticity) {
            processedElasticCollision = new HashSet<>();
        }
        for (int i = 0; i < positionX.length; i++) {
            if (!elasticity && deleted[i]) {
                continue;
            }
                for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }

                if (elasticity) {
                    if (processedElasticCollision.contains(new AbstractMap.SimpleEntry<>(i, j))) {
                        continue;
                    }
                } else if (deleted[j]) {
                    continue;
                }

                float distance = calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);
                        if (distance < radius[i] + radius[j]) {    // if collide
                    if (elasticity) {
                        processTwoDimensionalCollision(i, j);
                        processedElasticCollision.add(new AbstractMap.SimpleEntry<>(i, j));
                        processedElasticCollision.add(new AbstractMap.SimpleEntry<>(j, i));
                    } else {
                            /* Objects merging */
                            int bigger;
                            int smaller;
                            if (mass[i] < mass[j]) {
                                bigger = j;
                                smaller = i;
                            } else {
                                bigger = i;
                                smaller = j;
                            }

                            deleted[smaller] = true;

                            /* Speed */
                            changeSpeedOnMerging(smaller, bigger);

                            /* Position */
                            changePositionOnMerging(smaller, bigger);

                            /* Color */
                            color[bigger] = calculateColor(smaller, bigger);

                            /* Volume (radius) */
                            radius[bigger] = calculateRadiusBasedOnNewVolumeAndDensity(smaller, bigger);

                            /* Mass */
                            mass[bigger] = mass[bigger] + mass[smaller];

                            if (i == smaller) {
                                /* If the current object is deleted stop processing it further. */
                                break;
                            }
                        }
                    }
                }
            }
        }

    /**
     * Because processCollisions() method does not run on GPU
     * we can remove this method and replace color encode/decode with java.awt.Color
     */
    private int calculateColor(int smaller, int bigger) {
        float bigVolume = calculateVolumeFromRadius(radius[bigger]);
        float smallVolume = calculateVolumeFromRadius(radius[smaller]);

        /* Decode color */
        int biggerRed = (color[bigger] >> 16) & 0xFF;
        int biggerGreen = (color[bigger] >> 8) & 0xFF;
        int biggerBlue = color[bigger] & 0xFF;

        int smallerRed = (color[smaller] >> 16) & 0xFF;
        int smallerGreen = (color[smaller] >> 8) & 0xFF;
        int smallerBlue = color[smaller] & 0xFF;

        /* Calculate new value */
        int r = (int) Math.round((biggerRed * bigVolume + smallerRed * smallVolume) / (bigVolume + smallVolume));
        int g = (int) Math.round((biggerGreen * bigVolume + smallerGreen * smallVolume) / (bigVolume + smallVolume));
        int b = (int) Math.round((biggerBlue * bigVolume + smallerBlue * smallVolume) / (bigVolume + smallVolume));

        /* Encode color */
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    /**
     * We calculate for sphere, not for circle, so in 2D volume may not look real.
     */
    public float calculateRadiusBasedOnNewVolumeAndDensity(int smaller, int bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        float smallVolume = calculateVolumeFromRadius(radius[smaller]);
        float smallDensity = mass[smaller] / smallVolume;
        float bigVolume = calculateVolumeFromRadius(radius[bigger]);
        float bigDensity = mass[bigger] / bigVolume;
        float newMass = mass[bigger] + mass[smaller];

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be,
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        float newDensity = (smallDensity * mass[smaller] + bigDensity * mass[bigger]) / newMass;
        float newVolume = newMass / newDensity;

        return calculateRadiusFromVolume(newVolume);
    }

    private void changePositionOnMerging(int smaller, int bigger) {
        float distanceX = positionX[bigger] - positionX[smaller];
        float distanceY = positionY[bigger] - positionY[smaller];

        float massRatio = mass[smaller] / mass[bigger];

        positionX[bigger] = positionX[bigger] - distanceX * massRatio / TWO;
        positionY[bigger] = positionY[bigger] - distanceY * massRatio / TWO;
    }

    private void changeSpeedOnMerging(int smaller, int bigger) {
        /* We want to get already updated speed for the current one (bigger), thus we use speedX and not readOnlySpeedX */
        float totalImpulseX = speedX[smaller] * mass[smaller] + speedX[bigger] * mass[bigger];
        float totalImpulseY = speedY[smaller] * mass[smaller] + speedY[bigger] * mass[bigger];
        float totalMass = mass[bigger] + mass[smaller];

        speedX[bigger] = totalImpulseX / totalMass;
        speedY[bigger] = totalImpulseY / totalMass;
    }

    public static float calculateDistance(float object1X, float object1Y, float object2X, float object2Y) {
        float x = object2X - object1X;
        float y = object2Y - object1Y;
        return (float)Math.sqrt(x * x + y * y);
    }

    public static float calculateForce(final float object1Mass, final float object2Mass, final float distance) {
        return GRAVITY * object1Mass * object2Mass / (distance * distance);
    }

    public static float calculateVolumeFromRadius(float radius) {
        // V = 4/3 * pi * r^3
        if (radius == 0) {
            return Float.MIN_VALUE;
        }
        return RATIO_FOUR_THREE * PI * (float)Math.pow(radius, 3);
    }

    public static float calculateRadiusFromVolume(float volume) {
        // V = 4/3 * pi * r^3
        if (volume == 0) {
            return Float.MIN_VALUE;
        }
        return (float)Math.cbrt(volume / (RATIO_FOUR_THREE * PI));
    }

    private void processTwoDimensionalCollision(int o1, int o2) {
        float v1x = speedX[o1];
        float v1y = speedY[o1];
        float v2x = speedX[o2];
        float v2y = speedY[o2];
        
        float o1x = positionX[o1];
        float o1y = positionY[o1];
        float o2x = positionX[o2];
        float o2y = positionY[o2];
        
        float o1m = mass[o1];
        float o2m = mass[o2];
        
        // v'1y = v1y - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1y, o1x, o2y, o2x) * (o1y-o2y)
        // v'2x = v2x - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2x, o2y, o1x, o1y) * (o2x-o1x)
        // v'2y = v2y - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2y, o2x, o1y, o1x) * (o2y-o1y)
        // v'1x = v1x - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        speedX[o1] = calculateSpeed(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m);
        speedY[o1] = calculateSpeed(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m);
        speedX[o2] = calculateSpeed(v2x, v2y, v1x, v1y, o2x, o2y, o1x, o1y, o2m, o1m);
        speedY[o2] = calculateSpeed(v2y, v2x, v1y, v1x, o2y, o2x, o1y, o1x, o2m, o1m);
    }

    private float calculateSpeed(float v1x, float v1y, float v2x, float v2y,
                                  float o1x, float o1y, float o2x, float o2y, float o1m, float o2m) {
        // v'1x = v1x - 2*o2m/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return v1x - 2 * o2m / (o1m + o2m)
                * dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y)
                / dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y)
                * (o1x - o2x);
    }

    private float dotProduct2D(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax - bx) * (cx - dx) + (ay - by) * (cy - dy);
    }

    /**
     * For testing only.
     */
    @Override
    public void processTwoDimensionalCollision(SimulationObject o1, SimulationObject o2) {
        speedX[0] = o1.getSpeed().getX().floatValue();
        speedY[0] = o1.getSpeed().getY().floatValue();
        speedX[1] = o2.getSpeed().getX().floatValue();
        speedY[1] = o2.getSpeed().getY().floatValue();
        
        positionX[0] = o1.getX().floatValue();
        positionY[0] = o1.getY().floatValue();
        positionX[1] = o2.getX().floatValue();
        positionY[1] = o2.getY().floatValue();
        
        mass[0] = o1.getMass().floatValue();
        mass[1] = o2.getMass().floatValue();
        
        processTwoDimensionalCollision(0, 1);
        
        o1.setSpeed(new TripleNumber(New.num(speedX[0]), New.num(speedY[0]), ZERO));
        o2.setSpeed(new TripleNumber(New.num(speedX[1]), New.num(speedY[1]), ZERO));
    }
}
