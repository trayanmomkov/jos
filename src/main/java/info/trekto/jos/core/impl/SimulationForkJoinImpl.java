package info.trekto.jos.core.impl;

import info.trekto.jos.C;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.formulas.NewtonGravity;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.util.Utils;
import info.trekto.jos.visualization.java2dgraphics.VisualizerImpl;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

import static info.trekto.jos.formulas.ScientificConstants.NANOSECONDS_IN_ONE_SECOND;
import static info.trekto.jos.util.Utils.*;

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
    private List<Flow.Subscriber<? super List<SimulationObject>>> subscribers;

    private void doIteration() throws InterruptedException {
        SimulationLogicImpl.objectsForRemoval = ConcurrentHashMap.newKeySet();
        auxiliaryObjects = deepCopy(objects);

        /* Distribute simulation objects per threads and start execution */
        new SimulationRecursiveAction(0, objects.size()).compute();

        /* Remove disappeared because of collision objects */
        auxiliaryObjects.removeAll(SimulationLogicImpl.objectsForRemoval);

        objects = auxiliaryObjects;
        if (C.prop.isSaveToFile()) {
            C.io.appendObjectsToFile(objects);
        }
    }

    @Override
    public long startSimulation() throws SimulationException {
        init();

        logger.info("\nStart simulation...");
        C.endText = "END.";
        long startTime = System.nanoTime();
        long previousTime = startTime;
        long endTime;

        try {
            for (int i = 0; C.prop.isInfiniteSimulation() || i < C.prop.getNumberOfIterations(); i++) {
                try {
                    if (C.hasToStop) {
                        C.hasToStop = false;
                        C.io.endFile();
                        C.endText = "Stopped!";
                        break;
                    }

                    iterationCounter = i + 1;

                    if (System.nanoTime() - previousTime >= NANOSECONDS_IN_ONE_SECOND * 2) {
                        showRemainingTime(i, System.nanoTime() - startTime, C.prop.getNumberOfIterations(), objects.size());
                        previousTime = System.nanoTime();
                    }

                    if (C.prop.isRealTimeVisualization() && i % C.prop.getPlayingSpeed() == 0) {
                        notifySubscribers();
                    }

                    doIteration();
                    if (C.prop.isRealTimeVisualization() && C.prop.getPlayingSpeed() < 0) {
                        Thread.sleep(-C.prop.getPlayingSpeed());
                    }
                } catch (InterruptedException e) {
                    logger.error("Concurrency failure. One of the threads interrupted in cycle " + i, e);
                }
            }

            notifySubscribersEnd();
            endTime = System.nanoTime();
        } finally {
            if (C.prop.isSaveToFile()) {
                C.io.endFile();
            }
        }


        logger.info(String.format("End of simulation. Time: %.2f s.", (endTime - startTime) / (double) NANOSECONDS_IN_ONE_SECOND));
        return endTime - startTime;
    }

    private void notifySubscribers() {
        if (subscribers != null) {
            for (Flow.Subscriber<? super List<SimulationObject>> subscriber : subscribers) {
                subscriber.onNext(objects);
            }
        }
    }

    private void notifySubscribersEnd() {
        if (subscribers != null) {
            for (Flow.Subscriber<? super List<SimulationObject>> subscriber : subscribers) {
                subscriber.onComplete();
            }
        }
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
    public int getCurrentIterationNumber() {
        return iterationCounter;
    }

    @Override
    public ForceCalculator getForceCalculator() {
        return forceCalculator;
    }

    @Override
    public List<Flow.Subscriber<? super List<SimulationObject>>> getSubscribers() {
        return subscribers;
    }

    @Override
    public void removeAllSubscribers() {
        if (subscribers != null) {
            for (Flow.Subscriber<? super List<SimulationObject>> subscriber : subscribers) {
                if (subscriber instanceof VisualizerImpl) {
                    ((VisualizerImpl) subscriber).closeWindow();
                }
            }
        }
        subscribers = new ArrayList<>();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super List<SimulationObject>> subscriber) {
        if (subscribers == null) {
            subscribers = new ArrayList<>();
        }
        subscribers.add(subscriber);
    }
}
