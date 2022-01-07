package info.trekto.jos.core.impl;

import info.trekto.jos.Container;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.CommonFormulas;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.formulas.NewtonGravity;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.numbers.Number;
import info.trekto.jos.util.Utils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * @date 2017-May-18
 */
public class SimulationForkJoinImpl extends Observable implements Simulation {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimulationForkJoinImpl.class);

    // private Logger logger = LoggerFactory.getLogger(getClass());
//    private SimulationProperties properties = Container.properties;
    private Thread[] threads;
    private Map<Integer, ArrayList<Integer>> numberOfobjectsDistributionPerThread = new HashMap<>();
    private int iterationCounter;
    private ForceCalculator forceCalculator;

    /**
     * We cannot use array and just mark objects as disappeared because distribution per threads
     * will not work - we will not be able to distribute objects equally per thread. We need to
     * remove objects from lists. We need second auxiliary list in which to store objects with new
     * values. When calculation finished just swap lists. Auxiliary list must contain another
     * objects (not just references to original ones). This is need because we need to keep original
     * values when calculating new values. This approach prevents creation of new objects in every
     * iteration. We create objects at the beginning of the simulation and after that only remove
     * objects when collision appear. Good candidate for implementation of the lists is LinkedList
     * because during simulation we will not add any new objects to the lists, nor we will access
     * themrandomly (via indices). We only remove from them, get sublists and iterate sequentially.
     * But getting sublist is done by indices in every iteration. On the other hand removing objects
     * happens relatively rarely so ArrayList is faster than LinkedList.
     */
    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;
//    private List<SimulationObject> objectsForRemoval;

    private void doIteration() throws InterruptedException {
        /**
         * Distribute simulation objects per threads and start execution
         */
        if (Container.properties.getNumberOfThreads() == 1) {
            Container.simulationLogic.calculateNewValues(this, 0, objects.size());
        } else {
            new SimulationRecursiveAction(0, objects.size()).compute();
        }

        /**
         * Remove disappeared because of collision objects
         */
//        auxiliaryObjects.removeAll(objectsForRemoval);
        /**
         * Swap lists
         */
        {
            List<SimulationObject> tempList = objects;
            objects = auxiliaryObjects;

            /**
             * Make size of auxiliary to match that of objects list. Objects in auxiliaryObjects now
             * have old values but they will be replaced in next iteration
             */
            auxiliaryObjects = tempList.subList(0, objects.size());
        }
        /**
         * Here (outside the scope of tempList) objects remaining only in tempList should be
         * candidates for garbage collection.
         */

        if (Container.properties.isSaveToFile() && !Container.properties.isBenchmarkMode()) {
            Container.readerWriter.appendObjectsToFile(objects);
        }
    }

    @Override
    public long startSimulation() throws SimulationException {
        init();

        logger.info("\nStart simulation...");
        long globalStartTime = System.nanoTime();
        long startTime = globalStartTime;
        long endTime;

        for (int i = 0; Container.properties.isInfiniteSimulation() || i < Container.properties.getNumberOfIterations(); i++) {
            try {
                iterationCounter = i + 1;

                if (i % 1000 == 0 && !Container.properties.isBenchmarkMode()) {
                    logger.info("Iteration " + i);
//                    if (Container.properties.isBenchmarkMode()) {
//                        endTime = System.nanoTime();
//                        long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
//                        // logger.info("Iteration " + i + "\t" + (duration / 1000000) + " ms");
//                        logger.info("\t" + (duration / 1000000) + " ms");
//                        startTime = System.nanoTime();
//                    }
                }

                if (Container.properties.isRealTimeVisualization() && i % Container.properties.getPlayingSpeed() == 0) {
                    setChanged();
                    notifyObservers(objects);
                }

                doIteration();

                // /** On every 100 iterations flush to disk */
                // if (i % 100 == 0) {
                // Container.Container.readerWriter.flushToDisk();
                // }
            } catch (InterruptedException e) {
                // logger.error("One of the threads interrupted in cycle " + i, e);
                logger.error("Concurrency failure", e);
            }
        }

        endTime = System.nanoTime();

        if (Container.properties.isSaveToFile() && !Container.properties.isBenchmarkMode()) {
            Container.readerWriter.endFile();
        }
        logger.info("End of simulation.");
        return endTime - globalStartTime;
    }

    private boolean collisionExists() {
        for (Object element : objects) {
            SimulationObject object = (SimulationObject) element;
            for (Object element2 : objects) {
                SimulationObject object2 = (SimulationObject) element2;
                if (object == object2) {
                    continue;
                }
                // distance between centres
                Number distance = CommonFormulas.calculateDistance(object, object2);

                if (distance.compareTo(object.getRadius().add(object2.getRadius())) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void init() throws SimulationException {
        logger.info("Initialize simulation...");

        /**
         * This is need because we don't know the type of secondsPerItaration field before number
         * factory is set
         */
        Container.properties.setNanoSecondsPerIteration(Container.properties.getNanoSecondsPerIteration());

        switch (Container.properties.getForceCalculatorType()) {
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

        threads = new Thread[Container.properties.getNumberOfThreads()];
        objects = new ArrayList<SimulationObject>();
        auxiliaryObjects = new ArrayList<SimulationObject>();
//        objectsForRemoval = new ArrayList<SimulationObject>();

        for (SimulationObject simulationObject : Container.properties.getInitialObjects()) {
            objects.add(new SimulationObjectImpl(simulationObject));
            auxiliaryObjects.add(new SimulationObjectImpl(simulationObject));
        }
        if (collisionExists()) {
            throw new SimulationException("Initial collision exists!");
        }
        logger.info("Done.\n");

//        Utils.printConfiguration(Container.properties);
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
    public int getCurrentIterationNumber() {
        return iterationCounter;
    }

    @Override
    public ForceCalculator getForceCalculator() {
        return forceCalculator;
    }
}
