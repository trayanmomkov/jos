package info.trekto.jos.core.impl;

import info.trekto.jos.C;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

import static info.trekto.jos.formulas.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;

/**
 * This implementation uses fork/join Java framework introduced in Java 7.
 *
 * @author Trayan Momkov
 * 2017-May-18
 */
public class SimulationForkJoinImpl implements Simulation {
    private static final Logger logger = LoggerFactory.getLogger(SimulationForkJoinImpl.class);

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
     * them randomly (via indices). We only remove from them, get sublists and iterate sequentially.
     * But getting sublist is done by indices in every iteration. On the other hand removing objects
     * happens relatively rarely so ArrayList is faster than LinkedList.
     */
    private List<SimulationObject> objects;
    private List<SimulationObject> auxiliaryObjects;
//    private List<SimulationObject> objectsForRemoval;
    private List<Flow.Subscriber<? super List<SimulationObject>>> subscribers;

    private void doIteration() throws InterruptedException {
        /* Distribute simulation objects per threads and start execution */
        new SimulationRecursiveAction(0, objects.size()).compute();

        /* Remove disappeared because of collision objects */
//        auxiliaryObjects.removeAll(objectsForRemoval);
        /* Swap lists */
        {
            List<SimulationObject> tempList = objects;
            objects = auxiliaryObjects;

            /* Make size of auxiliary to match that of objects list. Objects in auxiliaryObjects now
             * have old values but they will be replaced in next iteration */
            auxiliaryObjects = tempList.subList(0, objects.size());
        }
        /* Here (outside the scope of tempList) objects remaining only in tempList should be
         * candidates for garbage collection. */

        if (C.prop.isSaveToFile()) {
            C.io.appendObjectsToFile(objects);
        }
    }

    @Override
    public long startSimulation() throws SimulationException {
        init();

        logger.info("\nStart simulation...");
        long startTime = System.nanoTime();
        long endTime;

        try {
            for (int i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    iterationCounter = i + 1;

                    if (i % 1000 == 0) {
                        logger.info("Iteration " + i);
                    }

                    if (C.prop.isRealTimeVisualization() && i % C.prop.getPlayingSpeed() == 0) {
                        notifySubscribers();
                    }

                    doIteration();

                    // /** On every 100 iterations flush to disk */
                    // if (i % 100 == 0) {
                    // Container.Container.readerWriter.flushToDisk();
                    // }
                } catch (InterruptedException e) {
                    logger.error("Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            endTime = System.nanoTime();
        } finally {
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }


        logger.info(String.format("End of simulation. Time: %.2f %n s.", (endTime - startTime) / (double)NANOSECONDS_IN_ONE_SECOND));
        return endTime - startTime;
    }

    private void notifySubscribers() {
        for (Flow.Subscriber<? super List<SimulationObject>> subscriber : subscribers) {
            subscriber.onNext(objects);
        }
    }

    private boolean collisionExists() {
        for (SimulationObject object : objects) {
            for (SimulationObject object1 : objects) {
                if (object == object1) {
                    continue;
                }
                // distance between centres
                Number distance = CommonFormulas.calculateDistance(object, object1);

                if (distance.compareTo(object.getRadius().add(object1.getRadius())) < 0) {
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

        //    private SimulationProperties properties = Container.properties;
        objects = new ArrayList<>();
        auxiliaryObjects = new ArrayList<>();
//        objectsForRemoval = new ArrayList<SimulationObject>();

        for (SimulationObject simulationObject : C.prop.getInitialObjects()) {
            objects.add(new SimulationObjectImpl(simulationObject));
            auxiliaryObjects.add(new SimulationObjectImpl(simulationObject));
        }
        if (collisionExists()) {
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
    public int getCurrentIterationNumber() {
        return iterationCounter;
    }

    @Override
    public ForceCalculator getForceCalculator() {
        return forceCalculator;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super List<SimulationObject>> subscriber) {
        if (subscribers == null) {
            subscribers = new ArrayList<>();
        }
        subscribers.add(subscriber);
    }
}
