package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.formulas.ForceCalculator;
import info.trekto.jos.core.formulas.NewtonGravity;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.util.Utils;
import info.trekto.jos.gui.java2dgraphics.VisualizerImpl;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.impl.SimulationLogicImpl.processCollisions;
import static info.trekto.jos.core.formulas.ScientificConstants.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.core.formulas.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.*;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationForkJoinImpl implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationForkJoinImpl.class);
    public static final int PAUSE_SLEEP_MILLISECONDS = 100;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;
    public boolean running = false;
    public boolean paused = false;

    private long iterationCounter;
    private ForceCalculator forceCalculator;

    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;


    private void doIteration(boolean saveCurrentIterationToFile) throws InterruptedException {
        auxiliaryObjects = deepCopy(objects);

        /* Distribute simulation objects per threads and start execution */
        new SimulationRecursiveAction(0, objects.size()).compute();

        /* Collision and merging */
        CollisionCheck.prepare();
        new CollisionCheck(0, auxiliaryObjects.size()).compute();
        
        /* If collision/s exists execute sequentially on a single thread */
        if (CollisionCheck.collisionExists()) {
            processCollisions(C.simulation);
        }

        objects = auxiliaryObjects;

        /* Slow down visualization */
        if (C.prop.isRealTimeVisualization() && C.prop.getPlayingSpeed() < 0) {
            Thread.sleep(-C.prop.getPlayingSpeed());
        }
        
        if (C.prop.isSaveToFile() && saveCurrentIterationToFile) {
            C.io.appendObjectsToFile(objects);
        }
    }

    @Override
    public void init(String inputFile) {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readProperties(inputFile);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot read properties file.", e);
        }
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void init(SimulationProperties prop) {
        C.io = new JsonReaderWriter();
        C.prop = prop;
        C.simulation = new SimulationForkJoinImpl();
    }

    public void initForPlaying(String inputFile) throws IOException {
        C.io = new JsonReaderWriter();
        try {
            C.prop = C.io.readPropertiesForPlaying(inputFile);
            C.prop.setRealTimeVisualization(true);
        } catch (FileNotFoundException e) {
            error(logger, "Cannot read properties file.", e);
        }
    }

    public void playSimulation(String inputFile) {
        try {
            // Only reset reader pointer. Do not change properties! We want to have the latest changes from the GUI.
            C.io.readPropertiesForPlaying(inputFile);
            C.simulation = this;
        } catch (IOException e) {
            error(logger, "Cannot reset input file for playing.", e);
        }
        C.visualizer = new VisualizerImpl();
        long previousTime = System.nanoTime();
        running = true;
        C.endText = "END.";
        try {
            while (C.io.hasMoreIterations()) {
                if (C.hasToStop) {
                    doStop();
                    break;
                }
                while (paused) {
                    Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                }
                Iteration iteration = C.io.readNextIteration();
                if (iteration == null) {
                    break;
                }

                if (C.prop.getPlayingSpeed() < 0) {
                    /* Slow down */
                    Thread.sleep(-C.prop.getPlayingSpeed());
                } else if ((System.nanoTime() - previousTime) / NANOSECONDS_IN_ONE_MILLISECOND < C.prop.getPlayingSpeed()) {
                    /* Speed up by not visualizing current iteration */
                    continue;
                }
                C.visualizer.visualize(iteration);
                previousTime = System.nanoTime();
                info(logger, "Cycle: " + iteration.getCycle() + ", number of objects: " + iteration.getNumberOfObjects());
            }
            info(logger, "End.");
            C.visualizer.end();
        } catch (IOException e) {
            error(logger, "Error while reading simulation object.", e);
        } catch (InterruptedException e) {
            error(logger, "Thread interrupted.", e);
        } finally {
            running = false;
        }
    }

    private void doStop() {
        C.hasToStop = false;
        if (C.prop.isSaveToFile()) {
            C.io.endFile();
        }
        if (C.visualizer != null) {
            C.endText = "Stopped!";
            C.visualizer.closeWindow();
        }
    }

    @Override
    public void startSimulation() throws SimulationException {
        init();

        info(logger, "Done.\n");
        Utils.printConfiguration(C.prop);

        info(logger, "Start simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        running = true;
        C.hasToStop = false;
        try {
            for (long i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        doStop();
                        break;
                    }
                    while (paused) {
                        Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, C.prop.getNumberOfIterations(), objects.size());
                        previousTime = System.nanoTime();
                    }

                    if (C.prop.isRealTimeVisualization() && System.nanoTime() - previousTime >= C.prop.getPlayingSpeed()) {
                        C.visualizer.visualize(objects);
                    }

                    doIteration(i % C.mainForm.getSaveEveryNthIteration() == 0);
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            if (C.prop.isRealTimeVisualization()) {
                C.visualizer.end();
            }
            endTime = System.nanoTime();
        } finally {
            running = false;
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }

        info(logger, "End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
    }

    private void init() throws SimulationException {
        logger.info("Initialize simulation...");

        /* This is need because we don't know the type of secondsPerItaration field before number
         * factory is set */
        C.prop.setSecondsPerIteration(C.prop.getSecondsPerIteration());

        switch (C.prop.getInteractingLaw()) {
            case NEWTON_LAW_OF_GRAVITATION:
                forceCalculator = new NewtonGravity();
                break;
            case COULOMB_LAW_ELECTRICALLY:
                throw new NotImplementedException("COULOMB_LAW_ELECTRICALLY is not implemented");
                // break;
            default:
                forceCalculator = new NewtonGravity();
                break;
        }

        objects = new ArrayList<>();

        for (SimulationObject simulationObject : C.prop.getInitialObjects()) {
            objects.add(new SimulationObjectImpl(simulationObject));
        }

        if (duplicateIdExists(objects)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(objects)) {
            throw new SimulationException("Initial collision exists!");
        }
        logger.info("Done.\n");

        Utils.printConfiguration(C.prop);
    }

    @Override
    public List<SimulationObject> getObjects() {
        return objects;
    }

    @Override
    public List<SimulationObject> getAuxiliaryObjects() {
        return auxiliaryObjects;
    }

    @Override
    public long getCurrentIterationNumber() {
        return iterationCounter;
    }

    @Override
    public ForceCalculator getForceCalculator() {
        return forceCalculator;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
    
    public void switchPause() {
        C.mainForm.switchPause();
    }
}
