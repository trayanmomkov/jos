package info.trekto.jos.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.util.Utils;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:36
 */
public class SimulationImpl implements Simulation {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimulationProperties properties;
    private int minimumObjectsPerThread;
    private Thread[] threads = new Thread[Utils.CORES];

    /**
     * We cannot use array and just to mark objects as disappeared because distribution per threads will not work - we
     * will not be able to distribute objects equally per thread. We need to remove objects from lists.
     * We need second auxiliary list in which to store objects with new values. When calculation finished just swap
     * lists. Auxiliary list must contain another objects (not just references to original ones). This is need because
     * we need to keep original values when calculating new values.
     * This approach prevents creation of new objects in every iteration. We create objects at the beginning of the
     * simulation and after that only remove objects when collision appear.
     */
    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;
    private List<SimulationObject> objectsForRemoval;

    private void doIteration() throws InterruptedException {
        /** Distribute simulation objects per threads and start execution */
        int fromIndex, toIndex;
        for (int i = 0; i < Utils.CORES; i++) {
            fromIndex = i * minimumObjectsPerThread;
            if (i == Utils.CORES - 1) {
                /** In last loop put all remaining objects to be processed by the last available thread. */
                toIndex = objects.size();
            } else {
                toIndex = i * minimumObjectsPerThread + minimumObjectsPerThread;
            }
            threads[i] = new Thread(
                    new SimulationRunnable(this, objects.subList(fromIndex, toIndex)));
            threads[i].start();
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
        /** Here objects remaining only in tempList must be candidates for garbage collection. */
    }

    @Override
    public void startSimulation() {
        init();
        for (long i = 0; i < properties.getNumberOfIterations(); i++) {
            try {
                doIteration();
            } catch (InterruptedException e) {
                logger.error("One of the threads interrupted in cycle " + i, e);
            }
        }
    }

    private void init() {
        logger.warn("init() not implemented");
        properties.setNumberOfObjects(100);
        objects = new ArrayList<SimulationObject>(properties.getN());
        minimumObjectsPerThread = properties.getN() / Utils.CORES;
        for (int i = 0; i < properties.getN(); i++) {
            objects.add(new SimulationObjectImpl());
            auxiliaryObjects.add(new SimulationObjectImpl());
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

}
