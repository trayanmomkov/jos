package info.trekto.jos.core.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.trekto.jos.Container;
import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:53:54
 */
public class SimulationRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<SimulationObject> objects;

    /** Index at which is the first object calculated by this thread */
    private int fromIndex;

    /** Index after index at which is the last object calculated by this thread i.e. toIndex is not included */
    private int toIndex;

    /**
     * @param objects
     * @param fromIndex Index at which is the first object calculated by this thread
     * @param toIndex Index after index at which is the last object calculated by this thread i.e. toIndex is not
     *            included
     */
    public SimulationRunnable(List<SimulationObject> objects, int fromIndex, int toIndex) {
        this.objects = objects;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public void run() {
        Container.getSimulationLogic().calculateNewValues(objects, objects.subList(fromIndex, toIndex));
    }

}
