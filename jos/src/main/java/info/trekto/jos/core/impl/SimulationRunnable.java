package info.trekto.jos.core.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.trekto.jos.Container;
import info.trekto.jos.core.Simulation;
import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:54
 */
public class SimulationRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<SimulationObject> targetObjects;
    private Simulation simulation;

    /**
     * @param objects
     * @param fromIndex Index at which is the first object calculated by this thread
     * @param toIndex Index after index at which is the last object calculated by this thread i.e. toIndex is not
     *            included
     */
    public SimulationRunnable(Simulation simulation, List<SimulationObject> targetObjects) {
        this.targetObjects = targetObjects;
        this.simulation = simulation;
    }

    @Override
    public void run() {
        Container.getSimulationLogic().calculateNewValues(simulation, targetObjects);
    }
}
