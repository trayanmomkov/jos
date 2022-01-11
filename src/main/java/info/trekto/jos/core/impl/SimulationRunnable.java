package info.trekto.jos.core.impl;

import info.trekto.jos.C;
import info.trekto.jos.core.Simulation;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:54
 */
public class SimulationRunnable implements Runnable {
    private int fromIndex;
    private int toIndex;
    private Simulation simulation;

    /**
     * @param fromIndex Index at which is the first object calculated by this thread
     * @param toIndex   Index after index at which is the last object calculated by this thread i.e. toIndex is not
     *                  included
     */
    public SimulationRunnable(Simulation simulation, int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
    }

    @Override
    public void run() {
        C.simulationLogic.calculateNewValues(simulation, fromIndex, toIndex);
    }
}
