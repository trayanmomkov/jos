package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.formulas.NewtonGravity;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.numbers.NumberFactoryProxy;
import info.trekto.jos.numbers.impl.BigDecimalNumberFactory;
import info.trekto.jos.numbers.impl.DoubleNumberFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:36
 */
public class SimulationImpl implements Simulation {

    //    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimulationProperties properties;
    private Thread[] threads;
    private Map<Integer, ArrayList<Integer>> numberOfobjectsDistributionPerThread = new HashMap<>();
    private int iterationCounter;
    private ForceCalculator forceCalculator;

    /**
     * We cannot use array and just to mark objects as disappeared because distribution per threads
     * will not work - we will not be able to distribute objects equally per thread. We need to
     * remove objects from lists. We need second auxiliary list in which to store objects with new
     * values. When calculation finished just swap lists. Auxiliary list must contain another
     * objects (not just references to original ones). This is need because we need to keep original
     * values when calculating new values. This approach prevents creation of new objects in every
     * iteration. We create objects at the beginning of the simulation and after that only remove
     * objects when collision appear. Good candidate for implementation of the lists is LinkedList
     * because during simulation we not add any new objects to the lists, nor we access them
     * randomly (via indices). We only remove from them, get sublists and iterate sequentially.
     * But getting sublist is done by indices in every iteration. On the other hand removing objects
     * happens relatively rarely so ArrayList is faster than LinkedList.
     */
    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;
    private List<SimulationObject> objectsForRemoval;

    private void doIteration() throws InterruptedException {
        /** Distribute simulation objects per threads and start execution */
        if (properties.getNumberOfThreads() == 1) {
            new SimulationRunnable(this, 0, objects.size()).run();
        } else {
            int fromIndex = 0, toIndex = 0;
            ArrayList<Integer> distributionPerThread = getObjectsDistributionPerThread(properties.getNumberOfThreads(),
                    objects.size());
            for (int i = 0; i < properties.getNumberOfThreads(); i++) {
                toIndex = fromIndex + distributionPerThread.get(i);
                threads[i] = new Thread(new SimulationRunnable(this, fromIndex, toIndex));
                threads[i].start();
                fromIndex = toIndex;
            }

            /** Wait for threads to finish */
            for (Thread thread : threads) {
                thread.join();
            }
        }

        /** Remove disappeared because of collision objects */
        auxiliaryObjects.removeAll(objectsForRemoval);

        /** Swap lists */
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

        if (properties.isSaveToFile()) {
            properties.getFormatVersion1Writer().appendObjectsToFile(objects);
        }
    }

    @Override
    public long startSimulation() {
        init();

        long globalStartTime = System.nanoTime();
        long startTime = globalStartTime;
        long endTime;

        for (int i = 0; properties.isInfiniteSimulation() || i < properties.getNumberOfIterations(); i++) {
            try {
                iterationCounter = i + 1;

                if (properties.isBenchmarkMode() && i % 10 == 0) {
                    endTime = System.nanoTime();
                    long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
                    //                    logger.info("Iteration " + i + "\t" + (duration / 1000000) + " ms");
                    System.out.println("Iteration " + i + "\t" + (duration / 1000000) + " ms");
                    startTime = System.nanoTime();
                }
                doIteration();

                //                /** On every 100 iterations flush to disk */
                //                if (i % 100 == 0) {
                //                    properties.getFormatVersion1Writer().flushToDisk();
                //                }
            } catch (InterruptedException e) {
                //                logger.error("One of the threads interrupted in cycle " + i, e);
                e.printStackTrace();
            }
        }

        endTime = System.nanoTime();

        properties.getFormatVersion1Writer().endFile();

        return endTime - globalStartTime;
    }

    private void init() {
        switch (properties.getNumberType()) {
            case DOUBLE:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
            case BIG_DECIMAL:
                NumberFactoryProxy.setFactory(new BigDecimalNumberFactory());
                break;
            default:
                NumberFactoryProxy.setFactory(new DoubleNumberFactory());
                break;
        }

        /** This is need because we don't know the type of secondsPerItaration field before number factory is set */
        properties.setNanoSecondsPerIteration(properties.getNanoSecondsPerIteration());

        switch (properties.getForceCalculatorType()) {
            case NEWTON_LAW_OF_GRAVITATION:
                forceCalculator = new NewtonGravity();
                break;
            case COULOMB_LAW_ELECTRICALLY:
                throw new NotImplementedException("COULOMB_LAW_ELECTRICALLY is not implemented");
                //                break;
            default:
                forceCalculator = new NewtonGravity();
                break;
        }

        threads = new Thread[properties.getNumberOfThreads()];
        objects = new ArrayList<SimulationObject>();
        auxiliaryObjects = new ArrayList<SimulationObject>();
        objectsForRemoval = new ArrayList<SimulationObject>();
        for (int i = 0; i < properties.getN(); i++) {
            SimulationObject object = properties.getFormatVersion1Writer().readObjectFromFile();
            objects.add(object);
            auxiliaryObjects.add(new SimulationObjectImpl(object));
        }
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
    public List<SimulationObject> getObjects() {
        return objects;
    }

    @Override
    public void setObjects(List<SimulationObject> objects) {
        this.objects = objects;
    }

    @Override
    public List<SimulationObject> getAuxiliaryObjects() {
        return auxiliaryObjects;
    }

    @Override
    public void setAuxiliaryObjects(List<SimulationObject> auxiliaryObjects) {
        this.auxiliaryObjects = auxiliaryObjects;
    }

    @Override
    public List<SimulationObject> getObjectsForRemoval() {
        return objectsForRemoval;
    }

    @Override
    public void setObjectsForRemoval(List<SimulationObject> objectsForRemoval) {
        this.objectsForRemoval = objectsForRemoval;
    }

    /**
     * Distribute objects per thread and return array which indices are thread numbers and values are objects for the
     * given thread.
     * For example: getObjectsDistributionPerThread(4, 10) must return [3, 3, 2, 2] that means
     * for thread 0 - 3 objects
     * for thread 1 - 3 objects
     * for thread 2 - 2 objects
     * for thread 3 - 2 objects
     * @param numberOfThreads
     * @param numberOfObjects
     * @return
     */
    public ArrayList<Integer> getObjectsDistributionPerThread(int numberOfThreads, int numberOfObjects) {
        if (numberOfobjectsDistributionPerThread.get(numberOfObjects) == null) {
            numberOfobjectsDistributionPerThread.put(numberOfObjects, new ArrayList<Integer>());
            for (int i = 0; i < numberOfThreads; i++) {
                numberOfobjectsDistributionPerThread.get(numberOfObjects).add(0);
            }
            int currentThread = 0;
            for (int i = 0; i < numberOfObjects; i++) {
                numberOfobjectsDistributionPerThread.get(numberOfObjects).set(
                        currentThread,
                        numberOfobjectsDistributionPerThread.get(numberOfObjects).get(currentThread) + 1);
                if (currentThread == numberOfThreads - 1) {
                    currentThread = 0;
                } else {
                    currentThread++;
                }
            }
        }
        return numberOfobjectsDistributionPerThread.get(numberOfObjects);
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
