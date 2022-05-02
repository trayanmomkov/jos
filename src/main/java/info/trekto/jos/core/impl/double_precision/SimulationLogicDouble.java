package info.trekto.jos.core.impl.double_precision;

import com.aparapi.Kernel;

public class SimulationLogicDouble extends Kernel {
    private static final double TWO = 2.0;
    private static final double RATIO_FOUR_THREE = 4 / 3.0;
    private static final double GRAVITY = 0.00000000006674; // 6.674×10^−11 N⋅m2/kg2
    private static final double PI = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862;

    public final double[] positionX;
    public final double[] positionY;
    public final double[] speedX;
    public final double[] speedY;
    public final double[] mass;
    public final double[] radius;
    public final String[] id;
    public final int[] color;
    public final boolean[] deleted;

    public final double[] readOnlyPositionX;
    public final double[] readOnlyPositionY;
    public final double[] readOnlySpeedX;
    public final double[] readOnlySpeedY;
    public final double[] readOnlyMass;
    public final double[] readOnlyRadius;
    public final int[] readOnlyColor;
    public final boolean[] readOnlyDeleted;

    private final double secondsPerIteration;

    public SimulationLogicDouble(int numberOfObjects, double secondsPerIteration) {
        int n = numberOfObjects;
        this.secondsPerIteration = secondsPerIteration;

        positionX = new double[n];
        positionY = new double[n];
        speedX = new double[n];
        speedY = new double[n];
        mass = new double[n];
        radius = new double[n];
        id = new String[n];
        color = new int[n];
        deleted = new boolean[n];

        readOnlyPositionX = new double[n];
        readOnlyPositionY = new double[n];
        readOnlySpeedX = new double[n];
        readOnlySpeedY = new double[n];
        readOnlyMass = new double[n];
        readOnlyRadius = new double[n];
        readOnlyColor = new int[n];
        readOnlyDeleted = new boolean[n];
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
            /* Calculate acceleration */
            double accelerationX = 0;
            double accelerationY = 0;
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
                    accelerationX = accelerationX + forceX / mass[i];
                    accelerationY = accelerationY + forceY / mass[i];
                }
            }

            /* Change speed */
            speedX[i] = speedX[i] + accelerationX * secondsPerIteration;
            speedY[i] = speedY[i] + accelerationY * secondsPerIteration;

            /* Move object */
            positionX[i] = positionX[i] + speedX[i] * secondsPerIteration;
            positionY[i] = positionY[i] + speedY[i] * secondsPerIteration;
        }
    }

    public void processCollisions() {
        for (int i = 0; i < positionX.length; i++) {
            if (!deleted[i]) {
                for (int j = 0; j < positionX.length; j++) {
                    if (i != j && !deleted[j]) {
                        double distance = calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);
                        if (distance < radius[i] + radius[j]) {    // if collide
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

    private void changeSpeedOnMerging(int smaller, int bigger) {
        /* We want to get already updated speed for the current one (bigger), thus we use speedX and not readOnlySpeedX */
        double totalImpulseX = speedX[smaller] * mass[smaller] + speedX[bigger] * mass[bigger];
        double totalImpulseY = speedY[smaller] * mass[smaller] + speedY[bigger] * mass[bigger];
        double totalMass = mass[bigger] + mass[smaller];

        speedX[bigger] = totalImpulseX / totalMass;
        speedY[bigger] = totalImpulseY / totalMass;
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
}
