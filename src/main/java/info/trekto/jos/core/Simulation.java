package info.trekto.jos.core;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Trayan Momkov
 * 2016-Mar-6
 */
public interface Simulation {

    void startSimulation() throws SimulationException;

    List<SimulationObject> getObjects();

    List<SimulationObject> getAuxiliaryObjects();

    long getCurrentIterationNumber();

    ForceCalculator getForceCalculator();

    void playSimulation(String absolutePath);

    SimulationProperties getProperties();

    void setProperties(SimulationProperties properties);

    Number calculateDistance(ImmutableSimulationObject object, ImmutableSimulationObject object1);
    
    boolean isCollisionExists();
    
    void upCollisionExists();
}
