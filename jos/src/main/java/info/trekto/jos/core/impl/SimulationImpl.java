package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:36
 */
public class SimulationImpl implements Simulation {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimulationProperties properties;
    private Thread[] threads = new Thread[Utils.CORES];
    private Map<Integer, ArrayList<Integer>> numberOfobjectsDistributionPerThread = new HashMap<>();
    private int iterationCounter;

    /**
     * We cannot use array and just to mark objects as disappeared because distribution per threads will not work - we
     * will not be able to distribute objects equally per thread. We need to remove objects from lists.
     * We need second auxiliary list in which to store objects with new values. When calculation finished just swap
     * lists. Auxiliary list must contain another objects (not just references to original ones). This is need because
     * we need to keep original values when calculating new values.
     * This approach prevents creation of new objects in every iteration. We create objects at the beginning of the
     * simulation and after that only remove objects when collision appear.
     * Good candidate for implementation of the lists is LinkedList because during simulation we not add any new objects
     * to the lists, nor we access them randomly (via indices). We only remove from them, get sublists and iterate
     * sequentially.
     */
    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;
    private List<SimulationObject> objectsForRemoval;

    private void doIteration() throws InterruptedException {
        /** Distribute simulation objects per threads and start execution */
        int fromIndex = 0, toIndex = 0;
        ArrayList<Integer> distributionPerThread = getObjectsDistributionPerThread(Utils.CORES, objects.size());
        for (int i = 0; i < Utils.CORES; i++) {
            toIndex = fromIndex + distributionPerThread.get(i);
            threads[i] = new Thread(
                    // new SimulationRunnable(this, objects.subList(fromIndex, toIndex)), "Thread " + i);
                    new SimulationRunnable(this, fromIndex, toIndex), "Thread " + i);
            threads[i].start();
            fromIndex = toIndex;
        }

        /** Wait for threads to finish */
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        /** Remove disappeared because of collision objects */
        auxiliaryObjects.removeAll(objectsForRemoval);

        /** Swap lists */
        {
            List<SimulationObject> tempList = objects;
            objects = auxiliaryObjects;

            /**
             * Make size of auxiliary to match that of objects list. Objects in auxiliaryObjects now have old values but
             * they will be replaced in next iteration
             */
            auxiliaryObjects = tempList.subList(0, objects.size());
        }
        /**
         * Here (outside the scope of tempList) objects remaining only in tempList should be candidates for garbage
         * collection.
         */

        properties.getFormatVersion1Writer().appendObjectsToFile(objects);
    }

    @Override
    public void startSimulation() {
        init();
        for (int i = 0; i < properties.getNumberOfIterations(); i++) {
            try {
                iterationCounter = i + 1;
                logger.info("\nIteration " + i);
                doIteration();
            } catch (InterruptedException e) {
                logger.error("One of the threads interrupted in cycle " + i, e);
            }
        }
    }

    private void init() {
        logger.warn("init() not implemented");
        objects = new LinkedList<SimulationObject>();
        auxiliaryObjects = new LinkedList<SimulationObject>();
        objectsForRemoval = new LinkedList<SimulationObject>();
        // optimalObjectsPerThread = properties.getN() / Utils.CORES;
        for (int i = 0; i < properties.getN(); i++) {
            SimulationObject object = new SimulationObjectImpl();
            object.setLabel("Obj " + i);
            object.setX(New.num(i));
            object.setY(New.num(i));
            object.setSpeed(new TripleNumber(New.num(1), New.num(0), New.num(0)));
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
}
