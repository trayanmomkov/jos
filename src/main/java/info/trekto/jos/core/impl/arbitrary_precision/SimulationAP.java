package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.SimulationLogic;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.ForceCalculator;
import info.trekto.jos.core.ScientificConstants;
import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.util.Utils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.ScientificConstants.NANOSECONDS_IN_ONE_MILLISECOND;
import static info.trekto.jos.core.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.*;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationAP implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationAP.class);
    public static final int PAUSE_SLEEP_MILLISECONDS = 100;
    public static final int SHOW_REMAINING_INTERVAL_SECONDS = 2;

    private SimulationLogic simulationLogic;
    private SimulationProperties properties;
    private ForceCalculator forceCalculator;
    private ScientificConstants scientificConstants;
    private long iterationCounter;

    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;

    public boolean collisionExists(List<SimulationObject> objects) {
        for (SimulationObject object : objects) {
            for (SimulationObject object1 : objects) {
                if (object == object1) {
                    continue;
                }
                // distance between centres
                Number distance = simulationLogic.calculateDistance(object, object1);

                if (distance.compareTo(object.getRadius().add(object1.getRadius())) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean duplicateIdExists(List<SimulationObject> objects) {
        Set<String> ids = new HashSet<>();
        for (SimulationObject object : objects) {
            if (!ids.add(object.getId())) {
                return true;
            }
        }
        return false;
    }

    private void doIteration(boolean saveCurrentIterationToFile) throws InterruptedException {
        auxiliaryObjects = deepCopy(objects);

        /* Distribute simulation objects per threads and start execution */
        new SimulationRecursiveAction(0, objects.size()).compute();

        /* Collision and merging */
        CollisionCheckRecursiveAction collisionCheck = new CollisionCheckRecursiveAction(0, auxiliaryObjects.size(), this);
        collisionCheck.prepare();
        collisionCheck.compute();

        /* If collision/s exists execute sequentially on a single thread */
        if (collisionCheck.collisionExists()) {
            simulationLogic.processCollisions(this);
        }

        objects = auxiliaryObjects;

        /* Slow down visualization */
        if (properties.isRealTimeVisualization() && properties.getPlayingSpeed() < 0) {
            Thread.sleep(-properties.getPlayingSpeed());
        }

        if (properties.isSaveToFile() && saveCurrentIterationToFile) {
            C.getReaderWriter().appendObjectsToFile(objects, properties, iterationCounter);
        }
    }

    public void playSimulation(String inputFile) {
        try {
            // Only reset reader pointer. Do not change properties! We want to have the latest changes from the GUI.
            C.getReaderWriter().readPropertiesForPlaying(inputFile);
        } catch (IOException e) {
            error(logger, "Cannot reset input file for playing.", e);
        }
        C.setVisualizer(C.createVisualizer(properties));
        long previousTime = System.nanoTime();
        C.setRunning(true);
        C.setEndText("END.");
        try {
            while (C.getReaderWriter().hasMoreIterations()) {
                if (C.hasToStop()) {
                    doStop();
                    break;
                }
                while (C.isPaused()) {
                    Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                }
                Iteration iteration = C.getReaderWriter().readNextIteration();
                if (iteration == null) {
                    break;
                }

                if (properties.getPlayingSpeed() < 0) {
                    /* Slow down */
                    Thread.sleep(-properties.getPlayingSpeed());
                } else if ((System.nanoTime() - previousTime) / NANOSECONDS_IN_ONE_MILLISECOND < properties.getPlayingSpeed()) {
                    /* Speed up by not visualizing current iteration */
                    continue;
                }
                C.getVisualizer().visualize(iteration);
                previousTime = System.nanoTime();
                info(logger, "Cycle: " + iteration.getCycle() + ", number of objects: " + iteration.getNumberOfObjects());
            }
            info(logger, "End.");
            C.getVisualizer().end();
        } catch (IOException e) {
            error(logger, "Error while reading simulation object.", e);
        } catch (InterruptedException e) {
            error(logger, "Thread interrupted.", e);
        } finally {
            C.setRunning(false);
        }
    }

    private void doStop() {
        C.setHasToStop(false);
        if (properties.isSaveToFile()) {
            C.getReaderWriter().endFile();
        }
        if (C.getVisualizer() != null) {
            C.setEndText("Stopped!");
            C.getVisualizer().closeWindow();
        }
    }

    @Override
    public void startSimulation() throws SimulationException {
        init();
        Utils.printConfiguration(properties);

        info(logger, "Start simulation...");
        C.setEndText("END.");
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        C.setRunning(true);
        C.setHasToStop(false);
        try {
            for (long i = 0; properties.isInfiniteSimulation() || i < properties.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop()) {
                        doStop();
                        break;
                    }
                    while (C.isPaused()) {
                        Thread.sleep(PAUSE_SLEEP_MILLISECONDS);
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * SHOW_REMAINING_INTERVAL_SECONDS) {
                        showRemainingTime(i, startTime, properties.getNumberOfIterations(), objects.size());
                        previousTime = System.nanoTime();
                    }

                    if (properties.isRealTimeVisualization() && System.nanoTime() - previousTime >= properties.getPlayingSpeed()) {
                        C.getVisualizer().visualize(objects);
                    }

                    doIteration(i % C.getSaveEveryNthIteration() == 0);
                } catch (InterruptedException e) {
                    error(logger, "Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            if (properties.isRealTimeVisualization()) {
                C.getVisualizer().end();
            }
            endTime = System.nanoTime();
        } finally {
            C.setRunning(false);
            if (properties.isSaveToFile()) {
                C.getReaderWriter().endFile();
            }
        }

        info(logger, "End of simulation. Time: " + nanoToHumanReadable(endTime - startTime));
    }

    private void init() throws SimulationException {
        logger.info("Initialize simulation...");

        /* This is need because we don't know the type of secondsPerItaration field before number
         * factory is set */
        properties.setSecondsPerIteration(properties.getSecondsPerIteration());

        switch (properties.getInteractingLaw()) {
            case NEWTON_LAW_OF_GRAVITATION:
                forceCalculator = new NewtonGravityAP();
                break;
            case COULOMB_LAW_ELECTRICALLY:
                throw new NotImplementedException("COULOMB_LAW_ELECTRICALLY is not implemented");
                // break;
            default:
                forceCalculator = new NewtonGravityAP();
                break;
        }

        objects = new ArrayList<>();

        for (SimulationObject simulationObject : properties.getInitialObjects()) {
            objects.add(new SimulationObjectImpl(simulationObject));
        }

        if (duplicateIdExists(objects)) {
            throw new SimulationException("Objects with duplicate IDs exist!");
        }

        if (collisionExists(objects)) {
            throw new SimulationException("Initial collision exists!");
        }
        logger.info("Done.\n");

        Utils.printConfiguration(properties);
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
    public void switchPause() {
        C.switchPause();
    }

    @Override
    public SimulationLogic getSimulationLogic() {
        return simulationLogic;
    }

    @Override
    public void setSimulationLogic(SimulationLogic simulationLogic) {
        this.simulationLogic = simulationLogic;
    }

    @Override
    public SimulationProperties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(SimulationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Number calculateDistance(ImmutableSimulationObject object, ImmutableSimulationObject object1) {
        return simulationLogic.calculateDistance(object, object1);
    }

    @Override
    public ScientificConstants getScientificConstants() {
        return scientificConstants;
    }

    @Override
    public void setScientificConstants(ScientificConstants scientificConstants) {
        this.scientificConstants = scientificConstants;
    }

    @Override
    public SimulationObject createNewSimulationObject() {
        return new SimulationObjectImpl();
    }

    @Override
    public SimulationObject createNewSimulationObject(SimulationObject o) {
        return new SimulationObjectImpl(o);
    }
}