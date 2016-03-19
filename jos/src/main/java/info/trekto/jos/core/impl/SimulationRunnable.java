package info.trekto.jos.core.impl;

import info.trekto.jos.Container;
import info.trekto.jos.core.Simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:54
 */
public class SimulationRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private int fromIndex;
    private int toIndex;
    private Simulation simulation;

    /**
     * @param objects
     * @param fromIndex Index at which is the first object calculated by this thread
     * @param toIndex Index after index at which is the last object calculated by this thread i.e. toIndex is not
     *            included
     */
    public SimulationRunnable(Simulation simulation, int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
    }

    @Override
    public void run() {
        Container.getSimulationLogic().calculateNewValues(simulation, fromIndex, toIndex);
    }
}
