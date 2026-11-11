package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.Data;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;

import java.util.List;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public interface Simulation {

    void startSimulation() throws SimulationException;
    
    void init(boolean printInfo) throws SimulationException;

    SimulationProperties getProperties();

    void doIteration(boolean saveCurrentIterationToFile, long iterationCounter) throws InterruptedException;

    double calculateTotalMass();

    double calculateTotalMomentum();
    
    Data getData();

    /* Gives back what was taken from the video card. The driver allows only a limited number of kernels per process. */
    default void dispose() {
    }

    /* The acceleration is force / mass and a merge needs the density, so every object needs a mass and a radius.
     * The objects can be of another number type than the selected one, so they are compared as double. */
    static void checkMassAndRadius(List<SimulationObject> objects) throws SimulationException {
        for (SimulationObject object : objects) {
            if (object.getMass().doubleValue() <= 0 || object.getRadius().doubleValue() <= 0) {
                throw new SimulationException("Object '" + object.getId() + "' has mass " + object.getMass()
                                                      + " and radius " + object.getRadius() + ". Both must be above zero.");
            }
        }
    }
}
