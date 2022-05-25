package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

public class SimulationLogicDouble extends Kernel implements SimulationLogic {
    private static final double TWO = 2.0;
    private static final double RATIO_FOUR_THREE = 4 / 3.0;
    private static final double GRAVITY = 0.000000000066743; // 6.6743×10^−11 N⋅m2/kg2
    private static final double PI = Math.PI;

    public final double[] positionX;
    public final double[] positionY;
    public final double[] velocityX;
    public final double[] velocityY;
    public final double[] accelerationX;
    public final double[] accelerationY;
    public final double[] mass;
    public final double[] radius;
    public final String[] id;
    public final int[] color;
    public final boolean[] deleted;

    public final double[] readOnlyPositionX;
    public final double[] readOnlyPositionY;
    public final double[] readOnlyVelocityX;
    public final double[] readOnlyVelocityY;
    public final double[] readOnlyAccelerationX;
    public final double[] readOnlyAccelerationY;
    public final double[] readOnlyMass;
    public final double[] readOnlyRadius;
    public final int[] readOnlyColor;
    public final boolean[] readOnlyDeleted;

    private final double secondsPerIteration;
    private final int screenWidth;
    private final int screenHeight;
    private final boolean mergeOnCollision;
    private final double coefficientOfRestitution;

    public SimulationLogicDouble(int numberOfObjects, double secondsPerIteration, int screenWidth, int screenHeight, boolean mergeOnCollision,
                                 double coefficientOfRestitution) {
        int n = numberOfObjects;
        this.secondsPerIteration = secondsPerIteration;

        positionX = new double[n];
        positionY = new double[n];
        velocityX = new double[n];
        velocityY = new double[n];
        accelerationX = new double[n];
        accelerationY = new double[n];
        mass = new double[n];
        radius = new double[n];
        id = new String[n];
        color = new int[n];
        deleted = new boolean[n];

        readOnlyPositionX = new double[n];
        readOnlyPositionY = new double[n];
        readOnlyVelocityX = new double[n];
        readOnlyVelocityY = new double[n];
        readOnlyAccelerationX = new double[n];
        readOnlyAccelerationY = new double[n];
        readOnlyMass = new double[n];
        readOnlyRadius = new double[n];
        readOnlyColor = new int[n];
        readOnlyDeleted = new boolean[n];
        
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mergeOnCollision = mergeOnCollision;
        this.coefficientOfRestitution = coefficientOfRestitution;
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
            double newAccelerationX = 0;
            double newAccelerationY = 0;
            for (int j = 0; j < readOnlyPositionX.length; j++) {
                if (i != j && !readOnlyDeleted[j]) {
                    /* Calculate force */
                    double distance = calculateDistance(positionX[i], positionY[i], readOnlyPositionX[j], readOnlyPositionY[j]);
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

            /* Move objects */
            /* For the time T, velocity moved the objects (changed their positions).
             * New objects positions are calculated having the velocity at the beginning of the period,
             * and these velocities are applied for time T. */
            positionX[i] = positionX[i] + velocityX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + velocityY[i] * secondsPerIteration;

            /* Change velocity */
            /* For the time T, accelerations changed the velocities.
             * Velocities are calculated having the accelerations of the objects at the beginning of the period,
             * and these accelerations are applied for time T. */
            velocityX[i] = velocityX[i] + accelerationX[i] * secondsPerIteration;
            velocityY[i] = velocityY[i] + accelerationY[i] * secondsPerIteration;
            
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
        if (positionX[i] + radius[i] >= screenWidth / 2.0 || positionX[i] - radius[i] <= -screenWidth / 2.0) {
            velocityX[i] = -velocityX[i];
        }

        if (positionY[i] + radius[i] >= screenHeight / 2.0 || positionY[i] - radius[i] <= -screenHeight / 2.0) {
            velocityY[i] = -velocityY[i];
        }
    }

    public void processCollisions() {
        Set<Map.Entry<Integer, Integer>> processedElasticCollision = null;
        if (!mergeOnCollision) {
            processedElasticCollision = new HashSet<>();
        }
        for (int i = 0; i < positionX.length; i++) {
            if (mergeOnCollision && deleted[i]) {
                continue;
            }
            for (int j = 0; j < positionX.length; j++) {
                if (i == j) {
                    continue;
                }

                if (mergeOnCollision) {
                    if (deleted[j]) {
                        continue;
                    }
                } else if (processedElasticCollision.contains(new AbstractMap.SimpleEntry<>(i, j))) {
                    continue;
                }

                double distance = calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);
                if (distance < radius[i] + radius[j]) {    // if collide
                    if (!mergeOnCollision) {
                        processTwoDimensionalCollision(i, j, coefficientOfRestitution);
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

                        /* Velocity */
                        changeVelocityOnMerging(smaller, bigger);

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
        double bigVolume = calculateVolumeFromRadius(radius[bigger]);
        double smallVolume = calculateVolumeFromRadius(radius[smaller]);

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
    public double calculateRadiusBasedOnNewVolumeAndDensity(int smaller, int bigger) {
        // density = mass / volume
        // calculate volume of smaller and add it to volume of bigger
        // calculate new radius of bigger based on new volume
        double smallVolume = calculateVolumeFromRadius(radius[smaller]);
        double smallDensity = mass[smaller] / smallVolume;
        double bigVolume = calculateVolumeFromRadius(radius[bigger]);
        double bigDensity = mass[bigger] / bigVolume;
        double newMass = mass[bigger] + mass[smaller];

        /* Volume and density are two sides of one coin. We should decide what we want one of them to be,
         * and calculate the other. Here we want the new object to have an average density of the two collided. */
        double newDensity = (smallDensity * mass[smaller] + bigDensity * mass[bigger]) / newMass;
        double newVolume = newMass / newDensity;

        return calculateRadiusFromVolume(newVolume);
    }

    private void changePositionOnMerging(int smaller, int bigger) {
        double distanceX = positionX[bigger] - positionX[smaller];
        double distanceY = positionY[bigger] - positionY[smaller];

        double massRatio = mass[smaller] / mass[bigger];

        positionX[bigger] = positionX[bigger] - distanceX * massRatio / TWO;
        positionY[bigger] = positionY[bigger] - distanceY * massRatio / TWO;
    }

    private void changeVelocityOnMerging(int smaller, int bigger) {
        /* We want to get already updated velocity for the current one (bigger), thus we use velocityX and not readOnlyVelocityX */
        double totalImpulseX = velocityX[smaller] * mass[smaller] + velocityX[bigger] * mass[bigger];
        double totalImpulseY = velocityY[smaller] * mass[smaller] + velocityY[bigger] * mass[bigger];
        double totalMass = mass[bigger] + mass[smaller];

        velocityX[bigger] = totalImpulseX / totalMass;
        velocityY[bigger] = totalImpulseY / totalMass;
    }

    public static double calculateDistance(double object1X, double object1Y, double object2X, double object2Y) {
        double x = object2X - object1X;
        double y = object2Y - object1Y;
        return Math.sqrt(x * x + y * y);
    }

    public static double calculateForce(final double object1Mass, final double object2Mass, final double distance) {
        return GRAVITY * object1Mass * object2Mass / (distance * distance);
    }

    public static double calculateVolumeFromRadius(double radius) {
        // V = 4/3 * pi * r^3
        if (radius == 0) {
            return Double.MIN_VALUE;
        }
        return RATIO_FOUR_THREE * PI * Math.pow(radius, 3);
    }

    public static double calculateRadiusFromVolume(double volume) {
        // V = 4/3 * pi * r^3
        if (volume == 0) {
            return Double.MIN_VALUE;
        }
        return Math.cbrt(volume / (RATIO_FOUR_THREE * PI));
    }
    
    private void processTwoDimensionalCollision(int o1, int o2, double cor) {
        double v1x = velocityX[o1];
        double v1y = velocityY[o1];
        double v2x = velocityX[o2];
        double v2y = velocityY[o2];
        
        double o1x = positionX[o1];
        double o1y = positionY[o1];
        double o2x = positionX[o2];
        double o2y = positionY[o2];
        
        double o1m = mass[o1];
        double o2m = mass[o2];
        
        // v'1y = v1y - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1y, o1x, o2y, o2x) * (o1y-o2y)
        // v'2x = v2x - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2x, o2y, o1x, o1y) * (o2x-o1x)
        // v'2y = v2y - 2*m2/(m1+m2) * dotProduct(o2, o1) / dotProduct(o2y, o2x, o1y, o1x) * (o2y-o1y)
        // v'1x = v1x - 2*m2/(m1+m2) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        velocityX[o1] = calculateVelocity(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y, o1m, o2m, cor);
        velocityY[o1] = calculateVelocity(v1y, v1x, v2y, v2x, o1y, o1x, o2y, o2x, o1m, o2m, cor);
        velocityX[o2] = calculateVelocity(v2x, v2y, v1x, v1y, o2x, o2y, o1x, o1y, o2m, o1m, cor);
        velocityY[o2] = calculateVelocity(v2y, v2x, v1y, v1x, o2y, o2x, o1y, o1x, o2m, o1m, cor);
    }

    private double calculateVelocity(double v1x, double v1y, double v2x, double v2y,
                                     double o1x, double o1y, double o2x, double o2y, double o1m, double o2m, double cor) {
        // v'1x = v1x - 2*o2m/(o1m+o2m) * dotProduct(o1, o2) / dotProduct(o1x, o1y, o2x, o2y) * (o1x-o2x)
        return v1x - (cor * o2m + o2m) / (o1m + o2m)
                * dotProduct2D(v1x, v1y, v2x, v2y, o1x, o1y, o2x, o2y)
                / dotProduct2D(o1x, o1y, o2x, o2y, o1x, o1y, o2x, o2y)
                * (o1x - o2x);
    }

    private double dotProduct2D(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        // <a - b, c - d> = (ax - bx) * (cx - dx) + (ay - by) * (cy - dy)
        return (ax - bx) * (cx - dx) + (ay - by) * (cy - dy);
    }

    /**
     * For testing only.
     */
    @Override
    public void processTwoDimensionalCollision(SimulationObject o1, SimulationObject o2, Number cor) {
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
        
        processTwoDimensionalCollision(0, 1, cor.doubleValue());
        
        o1.setVelocity(new TripleNumber(New.num(velocityX[0]), New.num(velocityY[0]), ZERO));
        o2.setVelocity(new TripleNumber(New.num(velocityX[1]), New.num(velocityY[1]), ZERO));
    }
}
