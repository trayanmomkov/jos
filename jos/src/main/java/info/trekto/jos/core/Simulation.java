package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:52:10
 */
public interface Simulation {

    void setProperties(SimulationProperties simulationProperties);

    SimulationProperties getProperties();

    void startSimulation();
}
