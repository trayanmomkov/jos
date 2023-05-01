package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.Data;
import info.trekto.jos.core.impl.SimulationProperties;

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
}
