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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import org.apache.commons.lang3.NotImplementedException;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * @date 2017-May-18
 */
public class SimulationForkJoinImpl extends Observable implements Simulation {

    // private Logger logger = LoggerFactory.getLogger(getClass());
    private SimulationProperties properties = Container.getProperties();
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
        if (properties.getNumberOfThreads() == 1) {
            Container.getSimulationLogic().calculateNewValues(this, 0, objects.size());
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

        if (properties.isSaveToFile() && !properties.isBenchmarkMode()) {
            properties.getFormatVersion1Writer().appendObjectsToFile(objects);
        }
    }

    @Override
    public long startSimulation() throws SimulationException {
        init();

        Utils.log("\nStart simulation...");
        long globalStartTime = System.nanoTime();
        long startTime = globalStartTime;
        long endTime;

        for (int i = 0; properties.isInfiniteSimulation() || i < properties.getNumberOfIterations(); i++) {
            try {
                iterationCounter = i + 1;

                if (properties.isBenchmarkMode() && i != 0 && i % 10000 == 0) {
                    endTime = System.nanoTime();
                    long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
                    // logger.info("Iteration " + i + "\t" + (duration / 1000000) + " ms");
                    Utils.log("Iteration " + i + "\t" + (duration / 1000000) + " ms");
                    startTime = System.nanoTime();
                }

                if (properties.isRealTimeVisualization() && i % properties.getPlayingSpeed() == 0) {
                    setChanged();
                    notifyObservers(objects);
                }

                doIteration();

                // /** On every 100 iterations flush to disk */
                // if (i % 100 == 0) {
                // properties.getFormatVersion1Writer().flushToDisk();
                // }
            } catch (InterruptedException e) {
                // logger.error("One of the threads interrupted in cycle " + i, e);
                e.printStackTrace();
            }
        }

        endTime = System.nanoTime();

        if (properties.isSaveToFile() && !properties.isBenchmarkMode()) {
            properties.getFormatVersion1Writer().endFile();
        }
        Utils.log("End of simulation.");
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
        Utils.log("Initialize simulation...");

        /**
         * This is need because we don't know the type of secondsPerItaration field before number
         * factory is set
         */
        properties.setNanoSecondsPerIteration(properties.getNanoSecondsPerIteration());

        switch (properties.getForceCalculatorType()) {
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

        threads = new Thread[properties.getNumberOfThreads()];
        objects = new ArrayList<SimulationObject>();
        auxiliaryObjects = new ArrayList<SimulationObject>();
//        objectsForRemoval = new ArrayList<SimulationObject>();
        for (int i = 0; i < properties.getN(); i++) {
            SimulationObject object = properties.getFormatVersion1Writer().readObjectFromFile();
            objects.add(object);
            auxiliaryObjects.add(new SimulationObjectImpl(object));
        }
        if (collisionExists()) {
            throw new SimulationException("Initial collision exists!");
        }
        Utils.log("Done.\n");

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
    public int getCurrentIterationNumber() {
        return iterationCounter;
    }

    @Override
    public ForceCalculator getForceCalculator() {
        return forceCalculator;
    }
}
