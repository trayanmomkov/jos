package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.model.SimulationObject;

import java.util.List;

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
     */
    long startSimulation();

    List<SimulationObject> getObjects();

    void setObjects(List<SimulationObject> objects);

    List<SimulationObject> getAuxiliaryObjects();

    void setAuxiliaryObjects(List<SimulationObject> auxiliaryObjects);

    List<SimulationObject> getObjectsForRemoval();

    void setObjectsForRemoval(List<SimulationObject> objectsForRemoval);

    int getCurrentIterationNumber();
}
