package info.trekto.jos.core;

import java.util.List;
import java.util.Observer;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.model.SimulationObject;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:52:10
 */
public interface Simulation {

    void setProperties(SimulationProperties simulationProperties);

    SimulationProperties getProperties();

    /**
     *
     * @return nanoseconds of execution excluding init and closing file.
     * @throws SimulationException
     */
    long startSimulation() throws SimulationException;

    List<SimulationObject> getObjects();

    void setObjects(List<SimulationObject> objects);

    List<SimulationObject> getAuxiliaryObjects();

    void setAuxiliaryObjects(List<SimulationObject> auxiliaryObjects);

    List<SimulationObject> getObjectsForRemoval();

    void setObjectsForRemoval(List<SimulationObject> objectsForRemoval);

    int getCurrentIterationNumber();

    ForceCalculator getForceCalculator();

    void addObserver(Observer o);
}
