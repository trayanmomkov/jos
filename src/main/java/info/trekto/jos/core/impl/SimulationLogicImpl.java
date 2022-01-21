package info.trekto.jos.core.impl;

import info.trekto.jos.C;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static info.trekto.jos.formulas.CommonFormulas.*;
import static info.trekto.jos.formulas.NewtonGravity.calculateForce;
import static info.trekto.jos.formulas.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.*;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationLogicImpl {
    private static final Logger logger = LoggerFactory.getLogger(SimulationLogicImpl.class);
    private static final double TWO = 2.0;
    public static double RATIO_FOUR_THREE = 4 / 3.0;
    public static double BILLION = 1000000000;

    private int iterationCounter;

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
    public final String[] readOnlyLabel;
    public final int[] readOnlyColor;
    public final boolean[] readOnlyDeleted;

    public SimulationLogicImpl(int numberOfObjects) {
        int n = numberOfObjects;
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
        readOnlyLabel = new String[n];
        readOnlyColor = new int[n];
        readOnlyDeleted = new boolean[n];
    }

    public long startSimulation() throws SimulationException {
        if (duplicateIdExists(id)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }
    
        if (collisionExists(positionX, positionY, radius)) {
            throw new SimulationException("Initial collision exists!");
        }
        
        logger.info("Done.\n");
        Utils.printConfiguration(C.prop);

        logger.info("\nStart simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        try {
            for (int i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        C.hasToStop = false;
                        C.io.endFile();
                        C.endText = "Stopped!";
                        break;
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * 2) {
                        showRemainingTime(i, System.nanoTime() - startTime, C.prop.getNumberOfIterations(), positionX.length);
                        previousTime = System.nanoTime();
                    }

                    if (C.prop.isRealTimeVisualization() && System.nanoTime() - previousTime >= C.prop.getPlayingSpeed()) {
                        C.visualizer.visualize();
                    }

                    doIteration();
                } catch (InterruptedException e) {
                    logger.error("Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            if (C.prop.isRealTimeVisualization()) {
                C.visualizer.end();
            }
            endTime = System.nanoTime();
        } finally {
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }


        logger.info(String.format("End of simulation. Time: %.2f s.", (endTime - startTime) / (double) NANOSECONDS_IN_ONE_SECOND));
        return endTime - startTime;
    }

    private void doIteration() throws InterruptedException {
        deepCopy(positionX, readOnlyPositionX);
        deepCopy(positionY, readOnlyPositionY);
        deepCopy(speedX, readOnlySpeedX);
        deepCopy(speedY, readOnlySpeedY);
        deepCopy(mass, readOnlyMass);
        deepCopy(radius, readOnlyRadius);
        deepCopy(color, readOnlyColor);
        deepCopy(deleted, readOnlyDeleted);

        calculateNewValues();

        /* Collision and merging */
        for (int i = 0; i < positionX.length; i++) {
            if (!deleted[i]) {
                processCollisions(i);
            }
        }
        
        if (C.prop.isRealTimeVisualization() && C.prop.getPlayingSpeed() < 0) {
            Thread.sleep(-C.prop.getPlayingSpeed());
        }

        if (C.prop.isSaveToFile()) {
            C.io.appendObjectsToFile();
        }
    }

    public void calculateNewValues() {
        for (int i = 0; i < positionX.length; i++) {
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
                speedX[i] = speedX[i] + accelerationX * C.prop.getSecondsPerIteration();
                speedY[i] = speedY[i] + accelerationY * C.prop.getSecondsPerIteration();

                /* Move object */
                positionX[i] = positionX[i] + speedX[i] * C.prop.getSecondsPerIteration();
                positionY[i] = positionY[i] + speedY[i] * C.prop.getSecondsPerIteration();
            }
        }
    }

    private void processCollisions(int i) {
        for (int j = 0; j < positionX.length; j++) {
            if (i != j && !deleted[j]) {
                double distance = calculateDistance(positionX[i], positionY[i], positionX[j], positionY[j]);
                if (distance < radius[i] + radius[j]) {    // if collide
                    /* Objects merging */
                    int bigger = i;
                    int smaller = j;
                    if (mass[i] < mass[j]) {
                        bigger = j;
                        smaller = i;
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
                        break;  // TODO Aparapi doesn't support breaks
                    }
                }
            }
        }
    }

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

        /* Volume and density are two sides of one coin. We should decide what we want to be one of them
         * and calculate the other. Here we wanted the new object to have an average density of the two collided. */
        double newDensity = (smallDensity + bigDensity) / 2.0;
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

    public int getCurrentIterationNumber() {
        return iterationCounter;
    }
}
