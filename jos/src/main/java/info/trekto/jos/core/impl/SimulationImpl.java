package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.util.Utils;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:36
 */
public class SimulationImpl implements Simulation {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimulationProperties properties;
    private int optimalObjectsPerThread;
    private Thread[] threads = new Thread[Utils.CORES];

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
        int fromIndex, toIndex;
        for (int i = 0; i < Utils.CORES; i++) {
            fromIndex = i * optimalObjectsPerThread;
            if (i == Utils.CORES - 1) {
                /** In last loop put all remaining objects to be processed by the last available thread. */
                toIndex = objects.size();
            } else {
                toIndex = i * optimalObjectsPerThread + optimalObjectsPerThread;
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

        properties.getFormatVersion1Writer().appendObjectsToFile(objects);
    }

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
        properties.setNumberOfIterations(10);
        objects = new LinkedList<SimulationObject>();
        auxiliaryObjects = new LinkedList<SimulationObject>();
        objectsForRemoval = new LinkedList<SimulationObject>();
        optimalObjectsPerThread = properties.getN() / Utils.CORES;
        for (int i = 0; i < properties.getN(); i++) {
            objects.add(new SimulationObjectImpl());
            auxiliaryObjects.add(new SimulationObjectImpl());
        }
    }


    public SimulationProperties getProperties() {
        return properties;
    }


    public void setProperties(SimulationProperties properties) {
        this.properties = properties;
    }


    public List<SimulationObject> getObjects() {
        return objects;
    }


    public void setObjects(List<SimulationObject> objects) {
        this.objects = objects;
    }


    public List<SimulationObject> getAuxiliaryObjects() {
        return auxiliaryObjects;
    }


    public void setAuxiliaryObjects(List<SimulationObject> auxiliaryObjects) {
        this.auxiliaryObjects = auxiliaryObjects;
    }


    public List<SimulationObject> getObjectsForRemoval() {
        return objectsForRemoval;
    }


    public void setObjectsForRemoval(List<SimulationObject> objectsForRemoval) {
        this.objectsForRemoval = objectsForRemoval;
    }

}
