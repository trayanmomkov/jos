package info.trekto.jos.core.impl;

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

    private List<SimulationObject> objects;

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
            threads[i] = new Thread(new SimulationRunnable(objects, fromIndex, toIndex));
            threads[i].start();
        }

        /** Wait for threads to finish */
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
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
        minimumObjectsPerThread = properties.getN() / Utils.CORES;
        for (int i = 0; i < 100; i++) {
            objects.add(new SimulationObjectImpl());
        }
    }

    public SimulationProperties getProperties() {
        return properties;
    }

    public void setProperties(SimulationProperties properties) {
        this.properties = properties;
    }

}
